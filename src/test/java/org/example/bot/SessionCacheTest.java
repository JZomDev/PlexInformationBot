package org.example.bot;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SessionCacheTest
{
	@Test
	void servesTheCachedValueWithinTheTtlThenReloads()
	{
		AtomicLong clock = new AtomicLong(1000);
		AtomicInteger loads = new AtomicInteger();
		SessionCache<String> cache = new SessionCache<>(() -> "v" + loads.incrementAndGet(), clock::get, 2000);
		assertEquals("v1", cache.get());
		clock.addAndGet(1999);
		assertEquals("v1", cache.get());
		clock.addAndGet(1);
		assertEquals("v2", cache.get());
	}

	@Test
	void failedLoadCachesNullForTheTtl()
	{
		AtomicLong clock = new AtomicLong(1000);
		AtomicInteger loads = new AtomicInteger();
		SessionCache<String> cache = new SessionCache<>(() ->
		{
			loads.incrementAndGet();
			throw new IllegalStateException("down");
		}, clock::get, 2000);
		assertNull(cache.get());
		assertNull(cache.get());
		assertEquals(1, loads.get());
	}

	@Test
	void clearForcesAReload()
	{
		AtomicInteger loads = new AtomicInteger();
		SessionCache<String> cache = new SessionCache<>(() -> "v" + loads.incrementAndGet(), () -> 1000L, 2000);
		assertEquals("v1", cache.get());
		cache.clear();
		assertEquals("v2", cache.get());
	}

	@Test
	void callersDuringARunningRefreshGetThePreviousValueWithoutBlocking() throws Exception
	{
		AtomicLong clock = new AtomicLong(1000);
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		AtomicInteger loads = new AtomicInteger();
		SessionCache<String> cache = new SessionCache<>(() ->
		{
			int n = loads.incrementAndGet();
			if (n == 2)
			{
				started.countDown();
				release.await();
			}
			return "v" + n;
		}, clock::get, 2000);
		assertEquals("v1", cache.get());
		clock.addAndGet(5000);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try
		{
			Future<String> slow = pool.submit(cache::get);
			assertTrue(started.await(5, TimeUnit.SECONDS));
			assertEquals("v1", cache.get()); // returns at once, does not wait for the blocked refresh
			assertEquals(2, loads.get());
			release.countDown();
			assertEquals("v2", slow.get(5, TimeUnit.SECONDS));
			assertEquals("v2", cache.get());
		}
		finally
		{
			release.countDown();
			pool.shutdownNow();
		}
	}

	@Test
	void refreshStartedBeforeClearIsAbandoned() throws Exception
	{
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		AtomicInteger loads = new AtomicInteger();
		SessionCache<String> cache = new SessionCache<>(() ->
		{
			int n = loads.incrementAndGet();
			if (n == 1)
			{
				started.countDown();
				release.await();
			}
			return "v" + n;
		}, () -> 1000L, 2000);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try
		{
			Future<String> old = pool.submit(cache::get);
			assertTrue(started.await(5, TimeUnit.SECONDS));
			cache.clear();
			assertEquals("v2", cache.get()); // a fresh load is not stuck behind the abandoned one
			release.countDown();
			assertEquals("v1", old.get(5, TimeUnit.SECONDS));
			assertEquals("v2", cache.get()); // the abandoned result did not overwrite the cache
		}
		finally
		{
			release.countDown();
			pool.shutdownNow();
		}
	}
}
