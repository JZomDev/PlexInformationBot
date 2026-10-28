package org.example.announce;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncedStoreTest
{
	@TempDir
	Path tmp;

	@Test
	void marksSurviveReload()
	{
		AnnouncedStore store = new AnnouncedStore(tmp);
		store.markBaselined("movie");
		store.markSeen("movie", "tt1");
		store.markSeen("season", "s1");
		AnnouncedStore again = new AnnouncedStore(tmp);
		assertTrue(again.baselined("movie"));
		assertFalse(again.baselined("season"));
		assertTrue(again.seen("movie", "tt1"));
		assertTrue(again.seen("season", "s1"));
		assertFalse(again.seen("movie", "s1"));
		assertFalse(again.wasLost());
	}

	@Test
	void freshDirIsNotBaselinedAndNotLost()
	{
		AnnouncedStore store = new AnnouncedStore(tmp);
		assertFalse(store.baselined("movie"));
		assertFalse(store.seen("movie", "x"));
		assertFalse(store.wasLost());
	}

	@Test
	void corruptFileIsLostWithoutThrowing() throws IOException
	{
		Files.writeString(tmp.resolve("announced.json"), "{not json");
		AnnouncedStore store = new AnnouncedStore(tmp);
		assertFalse(store.baselined("movie"));
		assertTrue(store.wasLost());
	}

	@Test
	void markSeenIsIdempotent()
	{
		AnnouncedStore store = new AnnouncedStore(tmp);
		store.markSeen("movie", "a");
		store.markSeen("movie", "a");
		assertTrue(store.seen("movie", "a"));
		assertTrue(new AnnouncedStore(tmp).seen("movie", "a"));
	}
}
