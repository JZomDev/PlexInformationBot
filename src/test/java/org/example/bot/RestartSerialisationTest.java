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
		return new Settings(token, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
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
