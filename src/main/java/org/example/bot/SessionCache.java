package org.example.bot;

import java.util.concurrent.Callable;
import java.util.function.LongSupplier;

/**
 * A short-lived cache around a slow loader. The monitor guards only the cached value and its timestamp,
 * never the load itself: while one caller is loading, others get the previous value (or null) at once,
 * so a blackholed backend cannot pile callers up behind it.
 */
public class SessionCache<T>
{
	private final Callable<T> loader;
	private final LongSupplier clock;
	private final long ttlMillis;
	private final Object lock = new Object();
	private T value;
	private long loadedAt;
	private boolean valid; // false until a load has been stored, and after clear()
	private boolean refreshing;
	private long generation; // bumped by clear() so a refresh that began earlier cannot store its result

	public SessionCache(Callable<T> loader, LongSupplier clock, long ttlMillis)
	{
		this.loader = loader;
		this.clock = clock;
		this.ttlMillis = ttlMillis;
	}

	/** Returns the cached value, reloading when stale. A failed load caches null for the TTL. */
	public T get()
	{
		long now = clock.getAsLong();
		long started;
		synchronized (lock)
		{
			if ((valid && now - loadedAt < ttlMillis) || refreshing)
			{
				return value;
			}
			refreshing = true;
			started = generation;
		}
		T loaded = null;
		try
		{
			loaded = loader.call();
		}
		catch (Exception e)
		{
			loaded = null;
		}
		finally
		{
			synchronized (lock)
			{
				if (generation == started)
				{
					refreshing = false;
					value = loaded;
					loadedAt = now;
					valid = true;
				}
			}
		}
		return loaded;
	}

	public void clear()
	{
		synchronized (lock)
		{
			value = null;
			valid = false;
			refreshing = false; // an older refresh in flight is abandoned; its result will be discarded
			generation++;
		}
	}
}
