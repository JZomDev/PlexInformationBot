package org.example.bot;

import org.example.announce.Announcer;
import org.example.config.Settings;

import java.util.Optional;

public interface BotController
{
	void restart(Settings effective);

	BotState state();

	/** True once the setup slash commands were registered for at least one server on the current connection. */
	default boolean setupCommandsRegistered()
	{
		return false;
	}

	/** The last announcement cycle's outcome on the current connection, if one has run. */
	default Optional<Announcer.Result> announcementResult()
	{
		return Optional.empty();
	}
}
