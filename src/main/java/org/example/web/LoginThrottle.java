package org.example.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LoginThrottle
{
	private static final int MAX_FAILURES = 5;
	private static final Duration BLOCK = Duration.ofSeconds(30);

	private static final class Entry
	{
		int failures;
		Instant blockedUntil;
	}

	private final Clock clock;
	private final Map<String, Entry> entries = new HashMap<>();

	public LoginThrottle(Clock clock)
	{
		this.clock = clock;
	}

	public synchronized boolean allowed(String key)
	{
		purge();
		Entry e = entries.get(key);
		return e == null || e.blockedUntil == null;
	}

	public synchronized void failure(String key)
	{
		purge();
		Entry e = entries.computeIfAbsent(key, k -> new Entry());
		if (e.blockedUntil != null)
		{
			return;
		}
		e.failures++;
		if (e.failures >= MAX_FAILURES)
		{
			e.blockedUntil = clock.instant().plus(BLOCK);
		}
	}

	public synchronized void success(String key)
	{
		entries.remove(key);
	}

	private void purge()
	{
		Instant now = clock.instant();
		entries.values().removeIf(e -> e.blockedUntil != null && !now.isBefore(e.blockedUntil));
	}
}
