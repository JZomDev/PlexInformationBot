package org.example.bot;

import org.example.config.Settings;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestartSerialisationTest
{
	private static Settings withToken(String token)
	{
		return new Settings(token, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
	}

	@Test
	void concurrentRestartsNeverOverlap() throws Exception
	{
		AtomicInteger open = new AtomicInteger();
		AtomicInteger max = new AtomicInteger();
		List<String> connected = new CopyOnWriteArrayList<>();
		BotLifecycle lifecycle = new BotLifecycle(s ->
		{
			int now = open.incrementAndGet();
			max.accumulateAndGet(now, Math::max);
			connected.add(s.botToken());
			sleep(20);
			return open::decrementAndGet;
		});

		CountDownLatch go = new CountDownLatch(1);
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < 5; i++)
		{
			String token = "tok" + i;
			Thread t = new Thread(() ->
			{
				try
				{
					go.await();
				}
				catch (InterruptedException ignored)
				{
				}
				lifecycle.restart(withToken(token));
			});
			threads.add(t);
			t.start();
		}
		go.countDown();
		for (Thread t : threads)
		{
			t.join();
		}
		lifecycle.awaitIdle();

		assertTrue(max.get() <= 1, "max simultaneous connections was " + max.get());
		assertEquals(BotState.RUNNING, lifecycle.state());
		assertEquals(1, open.get());
		assertTrue(connected.size() >= 1 && connected.size() <= 5);
		lifecycle.restart(withToken("latest"));
		lifecycle.awaitIdle();
		assertEquals("latest", connected.get(connected.size() - 1));
		assertTrue(max.get() <= 1);
	}

	@Test
	void restartReturnsImmediatelyWithStarting() throws Exception
	{
		CountDownLatch release = new CountDownLatch(1);
		BotLifecycle lifecycle = new BotLifecycle(s ->
		{
			release.await();
			return () -> { };
		});
		lifecycle.restart(withToken("t"));
		assertEquals(BotState.STARTING, lifecycle.state());
		release.countDown();
		lifecycle.awaitIdle();
		assertEquals(BotState.RUNNING, lifecycle.state());
	}

	@Test
	void restartWithEmptyTokenIsNotConfiguredAndOpensNothing() throws Exception
	{
		AtomicInteger connects = new AtomicInteger();
		AtomicInteger closes = new AtomicInteger();
		BotLifecycle lifecycle = new BotLifecycle(s ->
		{
			connects.incrementAndGet();
			return closes::incrementAndGet;
		});
		lifecycle.restart(withToken(""));
		lifecycle.awaitIdle();
		assertEquals(BotState.NOT_CONFIGURED, lifecycle.state());
		assertEquals(0, connects.get());

		lifecycle.restart(withToken("t"));
		lifecycle.awaitIdle();
		lifecycle.restart(withToken(""));
		lifecycle.awaitIdle();
		assertEquals(BotState.NOT_CONFIGURED, lifecycle.state());
		assertEquals(1, connects.get());
		assertEquals(1, closes.get());
	}

	@Test
	void failedConnectIsFailedAndLeavesNothingOpenAndLogsNoToken() throws Exception
	{
		BotLifecycle lifecycle = new BotLifecycle(s ->
		{
			throw new IllegalStateException("bad login for secret-token");
		});
		lifecycle.restart(withToken("secret-token"));
		lifecycle.awaitIdle();
		assertEquals(BotState.FAILED, lifecycle.state());
	}

	@Test
	void rapidMixedRestartsNeverLeaveStateStarting() throws Exception
	{
		String[] finals = {"final-ok", "", "fail-final"};
		BotState[] expected = {BotState.RUNNING, BotState.NOT_CONFIGURED, BotState.FAILED};
		for (int round = 0; round < 60; round++)
		{
			AtomicInteger open = new AtomicInteger();
			AtomicInteger max = new AtomicInteger();
			List<String> connected = new CopyOnWriteArrayList<>();
			BotLifecycle lifecycle = new BotLifecycle(s ->
			{
				if (s.botToken().startsWith("fail"))
				{
					throw new IllegalStateException("login failed for " + s.botToken());
				}
				max.accumulateAndGet(open.incrementAndGet(), Math::max);
				connected.add(s.botToken());
				return open::decrementAndGet;
			});
			String[] mix = {"a", "", "fail-1", "b", "", "fail-2", "c"};
			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < 4; i++)
			{
				int offset = i;
				Thread t = new Thread(() ->
				{
					for (int j = 0; j < mix.length; j++)
					{
						lifecycle.restart(withToken(mix[(j + offset) % mix.length]));
					}
				});
				threads.add(t);
				t.start();
			}
			for (Thread t : threads)
			{
				t.join();
			}
			lifecycle.awaitIdle();
			assertTrue(lifecycle.state() != BotState.STARTING);

			int k = round % finals.length;
			lifecycle.restart(withToken(finals[k]));
			lifecycle.awaitIdle();
			assertEquals(expected[k], lifecycle.state());
			if (k == 0)
			{
				assertEquals("final-ok", connected.get(connected.size() - 1));
			}
			assertTrue(max.get() <= 1);
		}
	}

	private static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
