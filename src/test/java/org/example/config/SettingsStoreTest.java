package org.example.config;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
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
				base.queueChannelId(), base.queueMessageId(), "", "", "");
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
				"1", "2", "3", "4", "5", "6", "7", "", "", "");
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
		Settings s = new Settings("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "", "", "");
		new SettingsStore(dir, Map.of()).save(s);
		assertFalse(Files.exists(dir.resolve("settings.json.tmp")));
		SettingsStore reread = new SettingsStore(dir, Map.of());
		assertEquals(s, reread.fileValues());
		assertEquals(s, reread.effective());
		assertTrue(reread.loadWarning().isEmpty());
	}

	@Test
	void concurrentUpdatesBothSurvive() throws Exception
	{
		for (int i = 0; i < 100; i++)
		{
			SettingsStore store = new SettingsStore(tmp.resolve("r" + i), Map.of());
			java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);
			Thread a = new Thread(() -> run(barrier, () -> store.update(s -> with(s, "tok", s.plexIp(), s.plexPort()))));
			Thread b = new Thread(() -> run(barrier, () -> store.update(s -> with(s, s.botToken(), "ip", s.plexPort()))));
			a.start();
			b.start();
			a.join();
			b.join();
			assertEquals("tok", store.fileValues().botToken());
			assertEquals("ip", store.fileValues().plexIp());
			assertEquals(store.fileValues(), new SettingsStore(tmp.resolve("r" + i), Map.of()).fileValues());
		}
	}

	private interface Action
	{
		void run() throws Exception;
	}

	private static void run(java.util.concurrent.CyclicBarrier barrier, Action action)
	{
		try
		{
			barrier.await();
			action.run();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	void failedUpdateLeavesStateUnchanged() throws IOException
	{
		Path file = Files.writeString(tmp.resolve("afile"), "x");
		SettingsStore store = new SettingsStore(file.resolve("sub"), Map.of());
		Settings before = store.fileValues();
		Settings effectiveBefore = store.effective();
		assertThrows(IOException.class, () -> store.update(s -> with(s, "tok", "ip", "1")));
		assertEquals(before, store.fileValues());
		assertEquals(effectiveBefore, store.effective());
	}

	@Test
	void normalisedHostsStripsSchemeSlashesAndWhitespace()
	{
		Settings in = new Settings("tok", "  HTTPS://1.2.3.4/ ", "32400", "ptok", "http://t:8181//", "tkey",
				" http://r:7878/ ", "rkey", "", "skey", "", "", "", "", "", "", "", "", "", "");
		Settings out = in.normalisedHosts();
		assertEquals("1.2.3.4", out.plexIp());
		assertEquals("t:8181", out.tautulliUrl());
		assertEquals("r:7878", out.radarrUrl());
		assertEquals("", out.sonarrUrl());
		assertEquals("tok", out.botToken());
		assertEquals("ptok", out.plexToken());
		assertEquals("tkey", out.tautulliApiKey());
		assertEquals("rkey", out.radarrApiKey());
		assertEquals("skey", out.sonarrApiKey());
		assertEquals("32400", out.plexPort());
		assertEquals("host:1", with(Settings.empty(), "", "host:1", "").normalisedHosts().plexIp());
		assertEquals("", Settings.empty().normalisedHosts().plexIp());
	}

	@Test
	void settingsAndCredentialsFilesAreOwnerOnly() throws IOException
	{
		Assumptions.assumeTrue(tmp.getFileSystem().supportedFileAttributeViews().contains("posix"));
		new SettingsStore(tmp, Map.of()).save(Settings.empty());
		new org.example.web.CredentialStore(tmp).change("jordan", "correct horse");
		Set<PosixFilePermission> ownerOnly = Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
		assertEquals(ownerOnly, Files.getPosixFilePermissions(tmp.resolve("settings.json")));
		assertEquals(ownerOnly, Files.getPosixFilePermissions(tmp.resolve("credentials.json")));
	}

	@Test
	void failedMoveLeavesNoTmpFile() throws IOException
	{
		Path blocker = Files.createDirectories(tmp.resolve("settings.json"));
		Files.writeString(blocker.resolve("child"), "x");
		SettingsStore store = new SettingsStore(tmp, Map.of());
		assertThrows(IOException.class, () -> store.save(Settings.empty()));
		assertFalse(Files.exists(tmp.resolve("settings.json.tmp")));
	}
}
