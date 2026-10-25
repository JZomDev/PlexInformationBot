package org.example.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest
{
	@Test
	void sessionExpiresAfterTtl()
	{
		MutableClock clock = new MutableClock();
		SessionManager sessions = new SessionManager(clock);
		SessionManager.Session s = sessions.create();
		clock.advance(Duration.ofHours(11));
		assertTrue(sessions.get(s.id()).isPresent());
		assertEquals(s.csrfToken(), sessions.get(s.id()).get().csrfToken());
		clock.advance(Duration.ofHours(2));
		assertTrue(sessions.get(s.id()).isEmpty());
	}

	@Test
	void destroyedSessionIsGone()
	{
		SessionManager sessions = new SessionManager(new MutableClock());
		SessionManager.Session s = sessions.create();
		sessions.destroy(s.id());
		assertTrue(sessions.get(s.id()).isEmpty());
		assertTrue(sessions.get("nope").isEmpty());
		assertTrue(sessions.get(null).isEmpty());
	}

	@Test
	void destroyAllRemovesEverySession()
	{
		SessionManager sessions = new SessionManager(new MutableClock());
		SessionManager.Session a = sessions.create();
		SessionManager.Session b = sessions.create();
		sessions.destroyAll();
		assertTrue(sessions.get(a.id()).isEmpty());
		assertTrue(sessions.get(b.id()).isEmpty());
	}

	@Test
	void idsAreUniqueAndLong()
	{
		SessionManager sessions = new SessionManager(new MutableClock());
		Set<String> ids = new HashSet<>();
		for (int i = 0; i < 100; i++)
		{
			SessionManager.Session s = sessions.create();
			assertTrue(s.id().length() >= 43);
			assertTrue(s.id().matches("[A-Za-z0-9_-]+"));
			assertNotEquals(s.id(), s.csrfToken());
			ids.add(s.id());
		}
		assertEquals(100, ids.size());
	}
}
