package org.example.announce;

import org.example.config.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementServiceTest
{
	@TempDir
	Path tmp;

	final AtomicInteger calls = new AtomicInteger();
	volatile RuntimeException boom;
	volatile Error boomError;

	final ArrSource source = new ArrSource()
	{
		@Override
		public boolean configured(String type)
		{
			return true;
		}

		@Override
		public List<Item> movies() throws IOException
		{
			calls.incrementAndGet();
			if (boomError != null)
			{
				throw boomError;
			}
			if (boom != null)
			{
				throw boom;
			}
			return List.of();
		}

		@Override
		public List<Item> seasons()
		{
			return List.of();
		}
	};

	private static Settings settings(String channel)
	{
		return new Settings("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", channel, "true",
				"true");
	}

	private AnnouncementService service(String channel)
	{
		Announcer a = new Announcer(source, i -> true, new AnnouncedStore(tmp), Clock.systemUTC());
		return new AnnouncementService(a, () -> settings(channel));
	}

	@Test
	void blankChannelDoesNothing()
	{
		AnnouncementService s = service("");
		assertDoesNotThrow(s::tick);
		assertEquals(0, calls.get());
	}

	@Test
	void tickRunsACycleWhenChannelSet()
	{
		AnnouncementService s = service("123");
		s.tick();
		assertEquals(1, calls.get());
		assertTrue(s.lastResult().isPresent());
	}

	@Test
	void sourceExceptionIsSwallowedAndReported()
	{
		boom = new IllegalStateException("secret-key-123");
		AnnouncementService s = service("123");
		assertDoesNotThrow(s::tick);
		assertTrue(s.lastResult().get().moviesFailed());
	}

	@Test
	void anErrorInACycleIsSwallowedSoTheScheduleSurvives()
	{
		boomError = new StackOverflowError();
		AnnouncementService s = service("123");
		assertDoesNotThrow(s::tick);
		boomError = null;
		s.tick();
		assertTrue(s.lastResult().isPresent());
	}

	@Test
	void settingsFailureIsSwallowed()
	{
		AnnouncementService s = new AnnouncementService(null, () -> {
			throw new IllegalStateException("x");
		});
		assertDoesNotThrow(s::tick);
	}
}
