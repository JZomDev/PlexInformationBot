package org.example.bot;

import org.example.config.Settings;

public interface BotController
{
	void restart(Settings effective);

	BotState state();

	/** True once the setup slash commands were registered for at least one server on the current connection. */
	default boolean setupCommandsRegistered()
	{
		return false;
	}
}
