package org.example.bot;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Remembers which servers already got their commands during one Discord connection. */
public class RegistrationTracker
{
	private final Set<Long> claimed = ConcurrentHashMap.newKeySet();

	/** True for the caller that should register this server; false when it already did or is doing so. */
	public boolean claim(long serverId)
	{
		return claimed.add(serverId);
	}

	/** Lets a later event retry after a failed registration or after the bot left the server. */
	public void release(long serverId)
	{
		claimed.remove(serverId);
	}
}
