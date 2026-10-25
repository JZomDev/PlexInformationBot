package org.example.web;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager
{
	public record Session(String id, String csrfToken, Instant expires)
	{
	}

	private static final Duration TTL = Duration.ofHours(12);
	private static final int BYTES = 32;

	private final Clock clock;
	private final SecureRandom random = new SecureRandom();
	private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

	public SessionManager(Clock clock)
	{
		this.clock = clock;
	}

	public Session create()
	{
		purgeExpired();
		Session session = new Session(token(), token(), clock.instant().plus(TTL));
		sessions.put(session.id(), session);
		return session;
	}

	public Optional<Session> get(String id)
	{
		purgeExpired();
		if (id == null)
		{
			return Optional.empty();
		}
		return Optional.ofNullable(sessions.get(id));
	}

	public void destroy(String id)
	{
		if (id != null)
		{
			sessions.remove(id);
		}
	}

	public void destroyAll()
	{
		sessions.clear();
	}

	private void purgeExpired()
	{
		Instant now = clock.instant();
		sessions.values().removeIf(s -> !now.isBefore(s.expires()));
	}

	private String token()
	{
		byte[] bytes = new byte[BYTES];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
