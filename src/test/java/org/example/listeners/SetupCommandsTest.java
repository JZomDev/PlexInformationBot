package org.example.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.Main;
import org.example.config.Settings;
import org.junit.jupiter.api.Test;

class SetupCommandsTest
{
	private static Settings base()
	{
		return new Settings("tok", "ip", "32400", "plex", "tu", "tk", "ru", "rk", "su", "sk",
				"r0", "rc0", "rm0", "sc0", "sm0", "qc0", "qm0");
	}

	@Test
	void eachCommandSetsOnlyItsOwnFields()
	{
		Settings b = base();
		Settings react = SetupCommandsListener.apply(b, "setupreact", "C", "M", "R");
		assertEquals(new Settings("tok", "ip", "32400", "plex", "tu", "tk", "ru", "rk", "su", "sk",
				"R", "C", "M", "sc0", "sm0", "qc0", "qm0"), react);
		Settings status = SetupCommandsListener.apply(b, "setupstatus", "C", "M", "R");
		assertEquals(new Settings("tok", "ip", "32400", "plex", "tu", "tk", "ru", "rk", "su", "sk",
				"r0", "rc0", "rm0", "C", "M", "qc0", "qm0"), status);
		Settings queue = SetupCommandsListener.apply(b, "setupcontentqueue", "C", "M", "R");
		assertEquals(new Settings("tok", "ip", "32400", "plex", "tu", "tk", "ru", "rk", "su", "sk",
				"r0", "rc0", "rm0", "sc0", "sm0", "C", "M"), queue);
	}

	@Test
	void rerunReplacesIdsAndKeepsOtherSettings()
	{
		Settings first = SetupCommandsListener.apply(base(), "setupstatus", "C1", "M1", "");
		Settings second = SetupCommandsListener.apply(first, "setupstatus", "C2", "M2", "");
		assertEquals("C2", second.statusChannelId());
		assertEquals("M2", second.statusMessageId());
		assertEquals("tok", second.botToken());
		assertEquals("qm0", second.queueMessageId());
		assertEquals("r0", second.roleId());
	}

	@Test
	void unknownCommandChangesNothing()
	{
		assertEquals(base(), SetupCommandsListener.apply(base(), "other", "C", "M", "R"));
	}

	@Test
	void matchesBySavedIdEvenIfTextDiffers()
	{
		Settings s = SetupCommandsListener.apply(Settings.empty(), "setupreact", "C", "42", "R");
		assertTrue(ReactListener.matchesRoleMessage(s, 7L, 1L, 42L, "edited text"));
	}

	@Test
	void matchesLegacyBotAuthoredTextWithoutSavedId()
	{
		assertTrue(ReactListener.matchesRoleMessage(Settings.empty(), 1L, 1L, 99L, Main.DISCORD_MESSAGE));
	}

	@Test
	void ignoresOtherUsersMessageWithSameText()
	{
		assertFalse(ReactListener.matchesRoleMessage(Settings.empty(), 7L, 1L, 99L, Main.DISCORD_MESSAGE));
	}

	@Test
	void emptySavedIdNeverMatchesZero()
	{
		assertFalse(ReactListener.matchesRoleMessage(Settings.empty(), 7L, 1L, 0L, "x"));
	}

	@Test
	void acceptsAnOrdinaryRoleTheBotCanManage()
	{
		assertEquals(null, SetupCommandsListener.roleProblem(false, false, true));
	}

	@Test
	void rejectsEveryoneManagedAndUnmanageableRolesWithFixedText()
	{
		assertEquals("That role cannot be used. Pick a normal role that is below the bot's own role.",
				SetupCommandsListener.roleProblem(true, false, true));
		assertEquals("That role cannot be used. Pick a normal role that is below the bot's own role.",
				SetupCommandsListener.roleProblem(false, true, true));
		assertEquals("That role cannot be used. Pick a normal role that is below the bot's own role.",
				SetupCommandsListener.roleProblem(false, false, false));
	}
}
