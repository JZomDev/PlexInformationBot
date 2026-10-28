package org.example.announce;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DiscordCardSinkTest
{
	private static Item item(String type, String title, int year, String summary)
	{
		return new Item(type, "k1", title, year, summary, "1080p - 4.2 GB", "https://img.example/p.jpg",
				Instant.parse("2026-01-01T00:00:00Z"));
	}

	/** javacord-core is runtime-only here, so the embed's JSON is read reflectively. */
	private static JsonObject json(Item item)
	{
		try
		{
			Object d = DiscordCardSink.embed(item).getDelegate();
			java.lang.reflect.Method m = d.getClass().getMethod("toJsonNode");
			return JsonParser.parseString(m.invoke(d).toString()).getAsJsonObject();
		}
		catch (ReflectiveOperationException e)
		{
			throw new AssertionError(e);
		}
	}

	@Test
	void movieCard()
	{
		JsonObject j = json(item(Announcer.MOVIE, "Heat", 1995, "A heist."));
		assertEquals("Heat (1995)", j.get("title").getAsString());
		assertEquals("New movie on Plex", j.getAsJsonObject("author").get("name").getAsString());
		assertEquals("A heist.", j.get("description").getAsString());
		assertEquals("1080p - 4.2 GB", j.getAsJsonObject("footer").get("text").getAsString());
		assertEquals("https://img.example/p.jpg", j.getAsJsonObject("thumbnail").get("url").getAsString());
		assertEquals(0xE5A00D, j.get("color").getAsInt() & 0xFFFFFF);
	}

	@Test
	void seasonCardHasNoYear()
	{
		JsonObject j = json(item(Announcer.SEASON, "Severance - Season 2", 2025, "Work."));
		assertEquals("Severance - Season 2", j.get("title").getAsString());
		assertEquals("Season complete on Plex", j.getAsJsonObject("author").get("name").getAsString());
	}

	@Test
	void summaryTruncatedTo300()
	{
		JsonObject j = json(item(Announcer.MOVIE, "Heat", 1995, "x".repeat(500)));
		assertEquals(300, j.get("description").getAsString().length());
	}

	@Test
	void nullSummaryIsFine()
	{
		JsonObject j = json(item(Announcer.MOVIE, "Heat", 1995, null));
		assertFalse(j.has("description"));
	}

	@Test
	void mentionsAreEmpty()
	{
		AllowedMentions m = DiscordCardSink.noMentions();
		assertTrue(m.getMentionTypes().isEmpty());
		assertTrue(m.getAllowedUserMentions().isEmpty());
		assertTrue(m.getAllowedRoleMentions().isEmpty());
		assertFalse(m.getMentionRepliedUser());
		JsonObject j = json(item(Announcer.MOVIE, "Heat", 1995, "hi @everyone"));
		assertEquals("hi @everyone", j.get("description").getAsString());
	}

	@Test
	void postReturnsFalseWhenNothingCanBeSent()
	{
		assertFalse(new DiscordCardSink(() -> {
			throw new IllegalStateException("no channel");
		}).post(item(Announcer.MOVIE, "Heat", 1995, "s")));
	}

	@Test
	void titleTruncatedTo256()
	{
		JsonObject j = json(item(Announcer.SEASON, "t".repeat(400), 0, "s"));
		assertEquals(256, j.get("title").getAsString().length());
	}

	@Test
	void truncationDoesNotSplitASurrogatePair()
	{
		String emoji = "\uD83D\uDE00";
		JsonObject j = json(item(Announcer.SEASON, "a".repeat(255) + emoji, 0, "b".repeat(299) + emoji));
		assertEquals(255, j.get("title").getAsString().length());
		assertEquals(299, j.get("description").getAsString().length());
	}

	@Test
	void interruptedSendCountsAsPostedAndKeepsTheInterruptFlag()
	{
		java.util.concurrent.atomic.AtomicInteger sends = new java.util.concurrent.atomic.AtomicInteger();
		DiscordCardSink sink = new DiscordCardSink(i -> {
			sends.incrementAndGet();
			return new java.util.concurrent.CompletableFuture<>();
		});
		Thread.currentThread().interrupt();
		try
		{
			assertTrue(sink.post(item(Announcer.MOVIE, "Heat", 1995, "s")));
			assertTrue(Thread.currentThread().isInterrupted());
			assertEquals(1, sends.get());
		}
		finally
		{
			Thread.interrupted();
		}
	}

	@Test
	void timedOutSendCountsAsPosted()
	{
		DiscordCardSink sink = new DiscordCardSink(i -> new java.util.concurrent.CompletableFuture<>(), 0);
		assertTrue(sink.post(item(Announcer.MOVIE, "Heat", 1995, "s")));
	}
}
