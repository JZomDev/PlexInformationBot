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
		assertEquals(new CheckResult(false, "The bot is not connected yet. Save the Discord token first."),
				p.probeChannel("111111111111111111"));
	}

	@Test
	void channelVerdictNamesTheChannelOnlyWhenTheBotCanPost()
	{
		assertEquals(new CheckResult(true, "Found #general. The bot can post here."),
				JavacordMessageProbe.channelVerdict("general", true, true, true));
		assertEquals(new CheckResult(false, "The bot cannot see that channel. Check the channel ID and the bot's permissions."),
				JavacordMessageProbe.channelVerdict("general", false, true, true));
		assertEquals(new CheckResult(false, "The bot is not allowed to send messages in #general"),
				JavacordMessageProbe.channelVerdict("general", true, false, true));
		assertEquals(new CheckResult(false, "The bot is not allowed to embed links in #general"),
				JavacordMessageProbe.channelVerdict("general", true, true, false));
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
