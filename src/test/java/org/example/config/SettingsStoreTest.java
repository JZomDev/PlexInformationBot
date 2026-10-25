package org.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettingsStoreTest
{
	@TempDir
	Path tmp;

	private static Settings with(Settings base, String botToken, String plexIp, String plexPort)
	{
		return new Settings(botToken, plexIp, plexPort, base.plexToken(), base.tautulliUrl(), base.tautulliApiKey(),
				base.radarrUrl(), base.radarrApiKey(), base.sonarrUrl(), base.sonarrApiKey(), base.roleId(),
				base.roleChannelId(), base.roleMessageId(), base.statusChannelId(), base.statusMessageId(),
				base.queueChannelId(), base.queueMessageId());
	}

	@Test
	void fileWinsPerField() throws IOException
	{
		Map<String, String> env = Map.of("IP", "1.1.1.1", "PORT", "32400");
		SettingsStore store = new SettingsStore(tmp, env);
		store.save(with(Settings.empty(), "", "2.2.2.2", ""));
		SettingsStore reread = new SettingsStore(tmp, env);
		assertEquals("2.2.2.2", reread.effective().plexIp());
		assertEquals("32400", reread.effective().plexPort());
	}

	@Test
	void envNamesMapToFields()
	{
		Map<String, String> env = Map.ofEntries(
				Map.entry("BOT_TOKEN", "a"), Map.entry("IP", "b"), Map.entry("PORT", "c"),
				Map.entry("PLEX_KEY", "d"), Map.entry("ROLE_ID", "e"), Map.entry("TEXT_CHANNELID", "f"),
				Map.entry("MESSAGEID", "g"), Map.entry("API_KEY", "h"), Map.entry("TAUTULLI_URL", "i"),
				Map.entry("QUEUE_TEXT_CHANNELID", "j"), Map.entry("QUEUEMESSAGEID", "k"),
				Map.entry("RADARR_URL", "l"), Map.entry("RADARR_API", "m"), Map.entry("SONARR_URL", "n"),
				Map.entry("SONARR_API", "o"));
		Settings s = Settings.fromEnv(env);
		assertEquals("a", s.botToken());
		assertEquals("b", s.plexIp());
		assertEquals("c", s.plexPort());
		assertEquals("d", s.plexToken());
		assertEquals("e", s.roleId());
		assertEquals("f", s.statusChannelId());
		assertEquals("g", s.statusMessageId());
		assertEquals("h", s.tautulliApiKey());
		assertEquals("i", s.tautulliUrl());
		assertEquals("j", s.queueChannelId());
		assertEquals("k", s.queueMessageId());
		assertEquals("l", s.radarrUrl());
		assertEquals("m", s.radarrApiKey());
		assertEquals("n", s.sonarrUrl());
		assertEquals("o", s.sonarrApiKey());
		assertEquals("", s.roleChannelId());
		assertEquals("", s.roleMessageId());
	}

	@Test
	void maskedHidesOnlyNonEmptySecrets()
	{
		Settings s = new Settings("tok", "ip", "p", "", "t", "key", "r", "", "s", "sk",
				"1", "2", "3", "4", "5", "6", "7");
		Settings m = s.masked();
		assertEquals("********", m.botToken());
		assertEquals("", m.plexToken());
		assertEquals("********", m.tautulliApiKey());
		assertEquals("", m.radarrApiKey());
		assertEquals("********", m.sonarrApiKey());
		assertEquals("ip", m.plexIp());
		assertEquals("t", m.tautulliUrl());
		assertEquals("7", m.queueMessageId());
	}

	@Test
	void untouchedMaskKeepsSavedSecret()
	{
		Settings saved = with(Settings.empty(), "old", "ip", "1");
		assertEquals("old", saved.applyForm(with(Settings.empty(), Settings.MASK, "ip", "1")).botToken());
		assertEquals("new", saved.applyForm(with(Settings.empty(), "new", "ip", "1")).botToken());
		assertEquals("", saved.applyForm(with(Settings.empty(), "", "ip", "1")).botToken());
	}

	@Test
	void corruptFileFallsBackToEnvAndWarns() throws IOException
	{
		Map<String, String> env = Map.of("IP", "1.1.1.1", "BOT_TOKEN", "t");
		Files.writeString(tmp.resolve("settings.json"), "{not json");
		SettingsStore store = new SettingsStore(tmp, env);
		assertEquals(Settings.fromEnv(env), store.effective());
		assertTrue(store.loadWarning().isPresent());
	}

	@Test
	void saveFailureThrowsIOException() throws IOException
	{
		Path file = Files.writeString(tmp.resolve("afile"), "x");
		SettingsStore store = new SettingsStore(file.resolve("sub"), Map.of());
		assertThrows(IOException.class, () -> store.save(Settings.empty()));
	}

	@Test
	void saveIsAtomicAndRoundTrips() throws IOException
	{
		Path dir = tmp.resolve("newdir");
		Settings s = new Settings("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q");
		new SettingsStore(dir, Map.of()).save(s);
		assertFalse(Files.exists(dir.resolve("settings.json.tmp")));
		SettingsStore reread = new SettingsStore(dir, Map.of());
		assertEquals(s, reread.fileValues());
		assertEquals(s, reread.effective());
		assertTrue(reread.loadWarning().isEmpty());
	}
}
