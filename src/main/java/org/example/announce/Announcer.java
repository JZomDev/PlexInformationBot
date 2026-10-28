package org.example.announce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** One poll cycle: baseline new types, mark off-type items seen, and post new arrivals. */
public final class Announcer
{
	public static final String MOVIE = "movie";
	public static final String SEASON = "season";
	static final int MAX_PER_CYCLE = 5;
	static final Duration BACKOFF_START = Duration.ofSeconds(30);
	static final Duration BACKOFF_MAX = Duration.ofMinutes(15);

	public record Result(int posted, boolean moviesFailed, boolean seasonsFailed, boolean sinkFailed)
	{
	}

	private record Retry(int failures, Instant notBefore)
	{
	}

	private final Logger logger = LogManager.getLogger(Announcer.class);
	private final ArrSource source;
	private final CardSink sink;
	private final AnnouncedStore store;
	private final Clock clock;

	// Kept in memory too, so an item already posted is never reposted if saving failed.
	private final Set<String> memorySeen = new HashSet<>();
	private final Set<String> memoryBaselined = new HashSet<>();
	private final Map<String, Retry> retries = new HashMap<>();
	private Set<String> activeCauses = new HashSet<>();
	private boolean lostWarned;

	public Announcer(ArrSource source, CardSink sink, AnnouncedStore store, Clock clock)
	{
		this.source = source;
		this.sink = sink;
		this.store = store;
		this.clock = clock;
	}

	public Result runCycle(boolean moviesOn, boolean seasonsOn, boolean channelSet)
	{
		if (!channelSet)
		{
			return new Result(0, false, false, false);
		}
		Set<String> causes = new HashSet<>();
		try
		{
			return cycle(moviesOn, seasonsOn, causes);
		}
		catch (RuntimeException e)
		{
			causes.add("cycle: " + e);
			logNewCauses(causes);
			return new Result(0, true, true, false);
		}
	}

	private Result cycle(boolean moviesOn, boolean seasonsOn, Set<String> causes)
	{
		boolean moviesConfigured = source.configured(MOVIE);
		boolean seasonsConfigured = source.configured(SEASON);
		// An unconfigured service is skipped entirely: nothing baselined, nothing marked seen, no failure.
		List<Item> movies = moviesConfigured ? fetch(MOVIE, causes) : null;
		List<Item> seasons = seasonsConfigured ? fetch(SEASON, causes) : null;

		List<Item> candidates = new ArrayList<>();
		collect(MOVIE, movies, moviesOn, candidates, causes);
		collect(SEASON, seasons, seasonsOn, candidates, causes);
		candidates.sort(Comparator.comparing(Item::added).thenComparing(Item::key));

		pruneRetries(candidates, movies != null || !moviesConfigured, seasons != null || !seasonsConfigured);
		Instant now = clock.instant();
		int posted = 0;
		int attempted = 0;
		boolean sinkFailed = false;
		for (Item item : candidates)
		{
			if (attempted >= MAX_PER_CYCLE || Thread.currentThread().isInterrupted())
			{
				break;
			}
			Retry retry = retries.get(id(item.type(), item.key()));
			if (retry != null && now.isBefore(retry.notBefore()))
			{
				continue;
			}
			attempted++;
			if (post(item))
			{
				posted++;
				retries.remove(id(item.type(), item.key()));
				markSeen(item.type(), item.key(), causes);
			}
			else
			{
				sinkFailed = true;
				int failures = retry == null ? 1 : retry.failures() + 1;
				retries.put(id(item.type(), item.key()), new Retry(failures, now.plus(backoff(failures))));
				causes.add("sink: failed to post announcements");
			}
		}
		logNewCauses(causes);
		// Items still waiting in backoff keep the failure showing so it does not flicker back to ok.
		return new Result(posted, movies == null && moviesConfigured, seasons == null && seasonsConfigured,
				sinkFailed || !retries.isEmpty());
	}

	/** Forgets backoff for items no longer waiting to be posted, unless their source could not be read. */
	private void pruneRetries(List<Item> candidates, boolean moviesRead, boolean seasonsRead)
	{
		Set<String> ids = new HashSet<>();
		for (Item item : candidates)
		{
			ids.add(id(item.type(), item.key()));
		}
		retries.keySet().removeIf(k -> !ids.contains(k)
				&& (k.startsWith(MOVIE + "|") ? moviesRead : seasonsRead));
	}

	/** Returns null when the source failed. */
	private List<Item> fetch(String type, Set<String> causes)
	{
		try
		{
			return MOVIE.equals(type) ? source.movies() : source.seasons();
		}
		catch (IOException | RuntimeException e)
		{
			causes.add(type + " source: " + e);
			return null;
		}
	}

	private void collect(String type, List<Item> items, boolean on, List<Item> candidates, Set<String> causes)
	{
		if (items == null)
		{
			return;
		}
		boolean baselined = isBaselined(type);
		if (!baselined && !on)
		{
			return;
		}
		if (!baselined && store.wasLost() && !lostWarned)
		{
			lostWarned = true;
			logger.warn("Announcement history was lost or unreadable; re-baselining without posting.");
		}
		List<String> toMark = new ArrayList<>();
		for (Item item : items)
		{
			if (item == null || isSeen(type, item.key()))
			{
				continue;
			}
			// At baseline every current item is marked seen, even blank ones.
			if (baselined && !valid(item))
			{
				continue;
			}
			if (!baselined || !on)
			{
				toMark.add(item.key());
			}
			else
			{
				candidates.add(item);
			}
		}
		markSeenAll(type, toMark, causes);
		if (!baselined)
		{
			markBaselined(type, causes);
		}
	}

	private boolean post(Item item)
	{
		try
		{
			return sink.post(item);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	private static boolean valid(Item item)
	{
		return item != null && item.title() != null && !item.title().isBlank()
				&& item.posterUrl() != null && !item.posterUrl().isBlank();
	}

	private static Duration backoff(int failures)
	{
		Duration d = BACKOFF_START;
		for (int i = 1; i < failures && d.compareTo(BACKOFF_MAX) < 0; i++)
		{
			d = d.multipliedBy(2);
		}
		return d.compareTo(BACKOFF_MAX) > 0 ? BACKOFF_MAX : d;
	}

	private static String id(String type, String key)
	{
		return type + "|" + key;
	}

	private boolean isSeen(String type, String key)
	{
		return memorySeen.contains(id(type, key)) || store.seen(type, key);
	}

	private boolean isBaselined(String type)
	{
		return memoryBaselined.contains(type) || store.baselined(type);
	}

	private void markSeen(String type, String key, Set<String> causes)
	{
		memorySeen.add(id(type, key));
		try
		{
			store.markSeen(type, key);
		}
		catch (UncheckedIOException e)
		{
			causes.add("store: could not save announced state: " + e.getCause());
		}
	}

	private void markSeenAll(String type, List<String> keys, Set<String> causes)
	{
		if (keys.isEmpty())
		{
			return;
		}
		for (String key : keys)
		{
			memorySeen.add(id(type, key));
		}
		try
		{
			store.markSeenAll(type, keys);
		}
		catch (UncheckedIOException e)
		{
			causes.add("store: could not save announced state: " + e.getCause());
		}
	}

	private void markBaselined(String type, Set<String> causes)
	{
		memoryBaselined.add(type);
		try
		{
			store.markBaselined(type);
		}
		catch (UncheckedIOException e)
		{
			causes.add("store: could not save announced state: " + e.getCause());
		}
	}

	/** Logs each cause once; a cause is logged again only after a cycle in which it did not occur. */
	private void logNewCauses(Set<String> causes)
	{
		for (String cause : causes)
		{
			if (!activeCauses.contains(cause))
			{
				logger.warn("Announcements: {}", cause);
			}
		}
		activeCauses = causes;
	}
}
