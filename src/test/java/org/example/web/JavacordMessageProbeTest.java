package org.example.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavacordMessageProbeTest
{
	@Test
	void notConnectedWhenThereIsNoLiveApi()
	{
		JavacordMessageProbe p = new JavacordMessageProbe(() -> null);
		assertEquals(new CheckResult(false, "The bot is not connected yet. Save the Discord token first."),
				p.probe("111111111111111111", "222222222222222222"));
		assertFalse(p.probeRole("333333333333333333", "111111111111111111").ok());
	}

	@Test
	void messageByTheBotIsUsable()
	{
		assertEquals(new CheckResult(true, "Found a message from PlexBot in #general; the bot can edit it"),
				JavacordMessageProbe.verdict(true, "PlexBot", "general"));
	}

	@Test
	void messageFromSomeoneElseIsRefused()
	{
		assertEquals(new CheckResult(false, "That message was written by someone else, so the bot cannot edit it"),
				JavacordMessageProbe.verdict(false, "PlexBot", "general"));
	}
}
