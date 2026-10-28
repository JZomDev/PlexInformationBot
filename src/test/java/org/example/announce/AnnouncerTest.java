package org.example.announce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncerTest
{
	private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

	@TempDir
	Path tmp;

	FakeSource source = new FakeSource();
	FakeSink sink = new FakeSink();
	MutableClock clock = new MutableClock();
	AnnouncedStore store;
	Announcer announcer;

	@BeforeEach
	void setUp()
	{
		store = new AnnouncedStore(tmp);
		announcer = new Announcer(source, sink, store, clock);
	}

	static final class FakeSource implements ArrSource
	{
		List<Item> movies = new ArrayList<>();
		List<Item> seasons = new ArrayList<>();
		boolean moviesThrow;
		boolean seasonsThrow;

		public List<Item> movies() throws IOException
		{
			if (moviesThrow)
			{
				throw new IOException("movies down");
			}
			return new ArrayList<>(movies);
		}

		public List<Item> seasons() throws IOException
		{
			if (seasonsThrow)
			{
				throw new IOException("seasons down");
			}
			return new ArrayList<>(seasons);
		}
	}

	static final class FakeSink implements CardSink
	{
		List<Item> posted = new ArrayList<>();
		Set<String> failKeys = new HashSet<>();
		List<String> attempts = new ArrayList<>();

		public boolean post(Item item)
		{
			attempts.add(item.key());
			if (failKeys.contains(item.key()))
			{
				return false;
			}
			posted.add(item);
			return true;
		}

		List<String> keys()
		{
			return posted.stream().map(Item::key).toList();
		}
	}

	static final class MutableClock extends Clock
	{
		Instant now = T0;

		public ZoneId getZone()
		{
			return ZoneOffset.UTC;
		}

		public Clock withZone(ZoneId zone)
		{
			return this;
		}

		public Instant instant()
		{
			return now;
		}

		void advance(Duration d)
		{
			now = now.plus(d);
		}
	}

	static Item movie(String key, int minute)
	{
		return item("movie", key, "Title " + key, "http://p/" + key, minute);
	}

	static Item season(String key, int minute)
	{
		return item("season", key, "Show " + key, "http://p/" + key, minute);
	}

	static Item item(String type, String key, String title, String poster, int minute)
	{
		return new Item(type, key, title, 2020, "sum", "det", poster, T0.plusSeconds(minute * 60L));
	}

	@Test
	void firstCycleBaselinesWithoutPosting()
	{
		source.movies.add(movie("m1", 1));
		source.seasons.add(season("s1", 2));
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertEquals(0, r.posted());
		assertTrue(sink.posted.isEmpty());
		assertTrue(store.baselined("movie"));
		assertTrue(store.baselined("season"));
		assertTrue(store.seen("movie", "m1"));
		assertTrue(store.seen("season", "s1"));
	}

	@Test
	void offTypesAreNotBaselinedButNewItemsAreMarkedSeenOnceBaselined()
	{
		announcer.runCycle(true, true, true);
		source.seasons.add(season("s1", 1));
		source.movies.add(movie("m1", 2));
		Announcer.Result r = announcer.runCycle(true, false, true);
		assertEquals(List.of("m1"), sink.keys());
		assertEquals(1, r.posted());
		assertTrue(store.seen("season", "s1"));
		announcer.runCycle(true, true, true);
		assertEquals(List.of("m1"), sink.keys());
	}

	@Test
	void laterArrivalsPostOnceOldestFirst()
	{
		announcer.runCycle(true, true, true);
		source.movies.add(movie("m2", 5));
		source.seasons.add(season("s1", 3));
		source.movies.add(movie("m1", 4));
		announcer.runCycle(true, true, true);
		assertEquals(List.of("s1", "m1", "m2"), sink.keys());
		announcer.runCycle(true, true, true);
		assertEquals(3, sink.posted.size());
	}

	@Test
	void maxFivePerCycleAcrossTypes()
	{
		announcer.runCycle(true, true, true);
		for (int i = 0; i < 4; i++)
		{
			source.movies.add(movie("m" + i, i * 2));
			source.seasons.add(season("s" + i, i * 2 + 1));
		}
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertEquals(5, r.posted());
		assertEquals(List.of("m0", "s0", "m1", "s1", "m2"), sink.keys());
		announcer.runCycle(true, true, true);
		assertEquals(List.of("m0", "s0", "m1", "s1", "m2", "s2", "m3", "s3"), sink.keys());
	}

	@Test
	void thirtyMoviesOverCyclesNoDuplicatesOldestFirst()
	{
		announcer.runCycle(true, true, true);
		for (int i = 0; i < 30; i++)
		{
			source.movies.add(movie(String.format("m%02d", i), 30 - i));
		}
		for (int c = 0; c < 8; c++)
		{
			announcer.runCycle(true, true, true);
		}
		assertEquals(30, sink.posted.size());
		assertEquals(30, new HashSet<>(sink.keys()).size());
		List<Instant> times = sink.posted.stream().map(Item::added).toList();
		assertEquals(times.stream().sorted().toList(), times);
	}

	@Test
	void failedPostRetriesAfterBackoffWithoutBlockingOthers()
	{
		announcer.runCycle(true, true, true);
		source.movies.add(movie("m1", 1));
		source.movies.add(movie("m2", 2));
		sink.failKeys.add("m1");
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertTrue(r.sinkFailed());
		assertEquals(List.of("m2"), sink.keys());
		assertFalse(store.seen("movie", "m1"));

		announcer.runCycle(true, true, true);
		assertEquals(1, sink.attempts.stream().filter("m1"::equals).count());

		clock.advance(Duration.ofSeconds(30));
		announcer.runCycle(true, true, true);
		assertEquals(2, sink.attempts.stream().filter("m1"::equals).count());

		clock.advance(Duration.ofSeconds(59));
		announcer.runCycle(true, true, true);
		assertEquals(2, sink.attempts.stream().filter("m1"::equals).count());
		clock.advance(Duration.ofSeconds(1));
		sink.failKeys.clear();
		r = announcer.runCycle(true, true, true);
		assertFalse(r.sinkFailed());
		assertEquals(List.of("m2", "m1"), sink.keys());
	}

	@Test
	void backoffCapsAtFifteenMinutes()
	{
		announcer.runCycle(true, true, true);
		source.movies.add(movie("m1", 1));
		sink.failKeys.add("m1");
		for (int i = 0; i < 12; i++)
		{
			announcer.runCycle(true, true, true);
			clock.advance(Duration.ofMinutes(15));
		}
		int before = sink.attempts.size();
		announcer.runCycle(true, true, true);
		assertEquals(before + 1, sink.attempts.size());
		clock.advance(Duration.ofMinutes(14));
		announcer.runCycle(true, true, true);
		assertEquals(before + 1, sink.attempts.size());
		clock.advance(Duration.ofMinutes(1));
		announcer.runCycle(true, true, true);
		assertEquals(before + 2, sink.attempts.size());
	}

	@Test
	void sourceFailureIsPerTypeAndNeverThrows()
	{
		announcer.runCycle(true, true, true);
		source.moviesThrow = true;
		source.seasons.add(season("s1", 1));
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertTrue(r.moviesFailed());
		assertFalse(r.seasonsFailed());
		assertEquals(List.of("s1"), sink.keys());

		source.moviesThrow = false;
		source.seasonsThrow = true;
		source.movies.add(movie("m1", 2));
		r = announcer.runCycle(true, true, true);
		assertFalse(r.moviesFailed());
		assertTrue(r.seasonsFailed());
		assertEquals(List.of("s1", "m1"), sink.keys());

		source.moviesThrow = true;
		r = announcer.runCycle(true, true, true);
		assertTrue(r.moviesFailed() && r.seasonsFailed());
		assertEquals(0, r.posted());
	}

	@Test
	void blankTitleOrPosterSkippedAndNotMarkedSeen()
	{
		announcer.runCycle(true, true, true);
		source.movies.add(item("movie", "a", " ", "http://p", 1));
		source.movies.add(item("movie", "b", "T", "", 2));
		source.movies.add(item("movie", "c", "T", null, 3));
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertEquals(0, r.posted());
		assertFalse(store.seen("movie", "a"));
		assertFalse(store.seen("movie", "b"));
		assertFalse(store.seen("movie", "c"));
		source.movies.set(1, item("movie", "b", "T", "http://p/b", 2));
		announcer.runCycle(true, true, true);
		assertEquals(List.of("b"), sink.keys());
	}

	@Test
	void lostStoreRebaselinesSilently() throws IOException
	{
		Files.writeString(tmp.resolve("announced.json"), "{garbage");
		store = new AnnouncedStore(tmp);
		assertTrue(store.wasLost());
		announcer = new Announcer(source, sink, store, clock);
		source.movies.add(movie("m1", 1));
		Announcer.Result r = announcer.runCycle(true, true, true);
		assertEquals(0, r.posted());
		assertTrue(store.seen("movie", "m1"));
		assertTrue(sink.posted.isEmpty());
	}

	@Test
	void channelNotSetDoesNothing()
	{
		source.movies.add(movie("m1", 1));
		Announcer.Result r = announcer.runCycle(true, true, false);
		assertEquals(0, r.posted());
		assertFalse(store.baselined("movie"));
		assertFalse(store.seen("movie", "m1"));
		announcer.runCycle(true, true, true);
		assertTrue(store.baselined("movie"));
		assertTrue(sink.posted.isEmpty());
	}

	@Test
	void postedItemNeverReposedWhenSaveFails() throws IOException
	{
		announcer.runCycle(true, true, true);
		source.movies.add(movie("m1", 1));
		// Make the store file unwritable by replacing it with a directory.
		Path file = tmp.resolve("announced.json");
		Files.delete(file);
		Files.createDirectory(file);
		Files.writeString(file.resolve("blocker"), "x");

		Announcer.Result r = announcer.runCycle(true, true, true);
		assertEquals(1, r.posted());
		announcer.runCycle(true, true, true);
		announcer.runCycle(true, true, true);
		assertEquals(List.of("m1"), sink.keys());
	}

	@Test
	void blankItemPresentAtBaselineIsNotAnnouncedWhenFixedLater()
	{
		source.movies.add(item("movie", "a", "T", "", 1));
		announcer.runCycle(true, true, true);
		assertTrue(store.seen("movie", "a"));
		source.movies.set(0, item("movie", "a", "T", "http://p/a", 1));
		announcer.runCycle(true, true, true);
		assertTrue(sink.posted.isEmpty());
	}

	@Test
	void runCycleNeverThrowsOnBadItem()
	{
		announcer.runCycle(true, true, true);
		source.movies.add(movie("m1", 1));
		source.movies.add(new Item("movie", "m2", "T", 2020, "s", "d", "http://p", null));
		Announcer.Result r = assertDoesNotThrow(() -> announcer.runCycle(true, true, true));
		assertTrue(r.moviesFailed());
		assertTrue(r.seasonsFailed());
		assertEquals(0, r.posted());
	}
}
