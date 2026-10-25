package org.example.bot;

import org.example.config.Settings;

public interface BotController
{
	void restart(Settings effective);

	BotState state();
}
