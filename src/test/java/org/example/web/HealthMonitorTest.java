package org.example.web;

import org.example.bot.BotState;
import org.example.config.Settings;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HealthMonitorTest
{
	private static final String PLEX_TOKEN = "plex-token-secret";

	private static class FakeChecks extends ConnectionChecks
	{
		final AtomicInteger calls = new AtomicInteger();
		final AtomicInteger discordCalls = new AtomicInteger();
		volatile CheckResult plex = new CheckResult(true, "Connected to Plex");
		volatile CountDownLatch gate;

		FakeChecks()
		{
			super(HttpClient.newHttpClient(), t -> new CheckResult(true, "unused"));
		}

		@Override
		public CheckResult plex(String ip, String port, String token)
		{
			calls.incrementAndGet();
			CountDownLatch g = gate;
			if (g != null)
			{
				try
				{
					g.await(10, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
			return plex;
		}

		@Override
		public CheckResult tautulli(String url, String key)
		{
			calls.incrementAndGet();
			return new CheckResult(false, "Rejected: check the key");
		}

		@Override
		public CheckResult discord(String token)
		{
			discordCalls.incrementAndGet();
			return new CheckResult(true, "must not be used");
		}
	}

	private static final class Manual implements java.util.concurrent.Executor
	{
		final List<Runnable> queued = new ArrayList<>();

		@Override
		public void execute(Runnable r)
		{
			queued.add(r);
		}

		void runAll()
		{
			List<Runnable> copy = new ArrayList<>(queued);
			queued.clear();
			copy.forEach(Runnable::run);
		}
	}

	private static Settings of(String bot, String tautulliUrl, String tautulliKey)
	{
		return new Settings(bot, "10.0.0.5", "32400", PLEX_TOKEN, tautulliUrl, tautulliKey, "", "", "", "", "", "", "",
				"", "", "", "");
	}

	private Settings settings = of("", "", "");
	private BotState bot = BotState.RUNNING;
	private long now = 1_000_000L;
	private final FakeChecks checks = new FakeChecks();
	private final Manual exec = new Manual();
	private final HealthMonitor monitor = new HealthMonitor(() -> settings, checks, () -> bot, exec, () -> now);

	private static String state(Map<String, HealthMonitor.Health> m, String key)
	{
		return m.get(key).state();
	}

	@Test
	void unknownUntilTheFirstResultThenCached()
	{
		assertEquals("unknown", state(monitor.snapshot(false), "plex"));
		exec.runAll();
		Map<String, HealthMonitor.Health> m = monitor.snapshot(false);
		assertEquals("ok", state(m, "plex"));
		assertEquals("Connected to Plex", m.get("plex").message());
		now += 59_000;
		monitor.snapshot(false);
		assertTrue(exec.queued.isEmpty());
		assertEquals(1, checks.calls.get());
		now += 1_000;
		monitor.snapshot(false);
		assertEquals(1, exec.queued.size());
	}

	@Test
	void onDemandRefreshHasATenSecondMinimum()
	{
		monitor.snapshot(false);
		exec.runAll();
		now += 9_000;
		monitor.snapshot(true);
		assertTrue(exec.queued.isEmpty());
		now += 1_000;
		monitor.snapshot(true);
		assertEquals(1, exec.queued.size());
	}

	@Test
	void sectionsThatAreNotConfiguredAreUnset()
	{
		Map<String, HealthMonitor.Health> m = monitor.snapshot(false);
		assertEquals("unset", state(m, "tautulli"));
		assertEquals("unset", state(m, "radarr"));
		assertEquals("unset", state(m, "sonarr"));
		settings = Settings.empty();
		m = monitor.snapshot(false);
		assertEquals("unset", state(m, "plex"));
		assertEquals("unset", state(m, "discord"));
	}

	@Test
	void aFailedCheckIsDownWithTheCheckMessage()
	{
		settings = of("", "10.0.0.6:8181", "tautulli-key");
		monitor.snapshot(false);
		exec.runAll();
		Map<String, HealthMonitor.Health> m = monitor.snapshot(false);
		assertEquals("down", state(m, "tautulli"));
		assertEquals("Rejected: check the key", m.get("tautulli").message());
	}

	@Test
	void messagesNeverCarryASecret()
	{
		checks.plex = new CheckResult(false, "Bad " + PLEX_TOKEN + " here");
		monitor.snapshot(false);
		exec.runAll();
		String message = monitor.snapshot(false).get("plex").message();
		assertFalse(message.contains(PLEX_TOKEN));
		assertTrue(message.contains("********"));
	}

	@Test
	void discordComesFromTheBotStateWithoutAProbe()
	{
		settings = of("some-token-value", "", "");
		bot = BotState.RUNNING;
		assertEquals("ok", state(monitor.snapshot(false), "discord"));
		bot = BotState.FAILED;
		assertEquals("down", state(monitor.snapshot(false), "discord"));
		bot = BotState.STARTING;
		assertEquals("unknown", state(monitor.snapshot(false), "discord"));
		bot = BotState.NOT_CONFIGURED;
		assertEquals("unset", state(monitor.snapshot(false), "discord"));
		exec.runAll();
		assertEquals(0, checks.discordCalls.get());
	}

	@Test
	void aChangeOfSettingsDropsStaleResultsAndAllowsAnImmediateRefresh()
	{
		monitor.snapshot(false);
		monitor.settingsChanged();
		exec.runAll();
		assertEquals("unknown", state(monitor.snapshot(false), "plex"));
		exec.runAll();
		assertEquals("ok", state(monitor.snapshot(false), "plex"));
	}

	@Test
	void aHungCheckDoesNotBlockTheCaller() throws Exception
	{
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try
		{
			HealthMonitor real = new HealthMonitor(() -> settings, checks, () -> bot, pool, System::currentTimeMillis);
			CountDownLatch gate = new CountDownLatch(1);
			checks.gate = gate;
			long started = System.nanoTime();
			assertEquals("unknown", real.snapshot(false).get("plex").state());
			assertEquals("unknown", real.snapshot(true).get("plex").state());
			assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2));
			gate.countDown();
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (!"ok".equals(real.snapshot(false).get("plex").state()) && System.nanoTime() < deadline)
			{
				Thread.sleep(20);
			}
			assertEquals("ok", real.snapshot(false).get("plex").state());
		}
		finally
		{
			pool.shutdownNow();
		}
	}
}
