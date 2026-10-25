package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import org.example.config.Settings;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class MainTest
{
	@Test
	void javaplexLoggerIsQuiet()
	{
		Main.configureLogging();
		assertEquals(Level.WARNING, Logger.getLogger("kekolab.javaplex").getLevel());
	}

	@Test
	void bindFailureKeepsAConfiguredBotRunning()
	{
		Settings withToken = new Settings("tok", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
		assertTrue(Main.continueWithoutSetupPage(withToken));
		assertFalse(Main.continueWithoutSetupPage(Settings.empty()));
	}
}
