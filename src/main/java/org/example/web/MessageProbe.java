package org.example.web;

/** Asks the running bot whether it can use a Discord message. Messages are fixed texts, never exception text. */
public interface MessageProbe
{
	CheckResult probe(String channelId, String messageId);

	/** Whether the role exists in the server of that channel and can be handed out by the bot. */
	default CheckResult probeRole(String roleId, String channelId)
	{
		return new CheckResult(false, "The role cannot be checked");
	}
}
