package org.example.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class BotInviteTest
{
	private static String tokenFor(String id)
	{
		String first = Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
		return first + ".GabcdE.abcdefghijklmnopqrstuvwxyz0123456789";
	}

	@Test
	void permissionsAreTheExactSum()
	{
		assertEquals(268520512L, BotInvite.PERMISSIONS);
		assertEquals(1024L + 2048L + 16384L + 65536L + 64L + 268435456L, BotInvite.PERMISSIONS);
	}

	@Test
	void validTokenGivesTheInviteUrl()
	{
		assertEquals("https://discord.com/oauth2/authorize?client_id=123456789012345678"
				+ "&scope=bot%20applications.commands&permissions=268520512",
				BotInvite.url(tokenFor("123456789012345678")));
	}

	@Test
	void garbageBlankAndBadIdsGiveNothing()
	{
		assertEquals("", BotInvite.url(null));
		assertEquals("", BotInvite.url(""));
		assertEquals("", BotInvite.url("   "));
		assertEquals("", BotInvite.url("not a token"));
		assertEquals("", BotInvite.url("%%%.abc.def"));
		assertEquals("", BotInvite.url(tokenFor("1234")));
		assertEquals("", BotInvite.url(tokenFor("123456789012345678901")));
		assertEquals("", BotInvite.url(tokenFor("12345678901234567x")));
	}
}
