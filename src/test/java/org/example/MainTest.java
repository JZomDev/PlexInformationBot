package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Level;
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
}
