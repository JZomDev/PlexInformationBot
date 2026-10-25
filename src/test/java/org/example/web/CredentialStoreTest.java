package org.example.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CredentialStoreTest
{
	@TempDir
	Path dir;

	@Test
	void defaultAdminWorksWhenNoFile()
	{
		CredentialStore store = new CredentialStore(dir);
		assertTrue(store.mustChange());
		assertTrue(store.verify("admin", "admin"));
		assertFalse(store.verify("admin", "x"));
	}

	@Test
	void changeCreatesFileAndStopsDefault() throws IOException
	{
		CredentialStore store = new CredentialStore(dir);
		store.change("jordan", "correct horse");
		assertFalse(store.mustChange());
		assertFalse(store.verify("admin", "admin"));
		assertTrue(store.verify("jordan", "correct horse"));
		assertFalse(store.verify("jordan", "wrong horse!"));
		assertFalse(store.verify("Jordan", "correct horse"));
		String text = Files.readString(dir.resolve("credentials.json"), StandardCharsets.UTF_8);
		assertFalse(text.contains("correct horse"));
		assertFalse(Files.exists(dir.resolve("credentials.json.tmp")));
		assertTrue(new CredentialStore(dir).verify("jordan", "correct horse"));
	}

	@Test
	void fileIsOwnerOnlyWhenPosix() throws IOException
	{
		new CredentialStore(dir).change("jordan", "correct horse");
		try
		{
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(dir.resolve("credentials.json"));
			assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), perms);
		}
		catch (UnsupportedOperationException ignored)
		{
		}
	}

	@Test
	void deletingFileRestoresDefault() throws IOException
	{
		CredentialStore store = new CredentialStore(dir);
		store.change("jordan", "correct horse");
		Files.delete(dir.resolve("credentials.json"));
		assertTrue(store.mustChange());
		assertTrue(store.verify("admin", "admin"));
		assertFalse(store.verify("jordan", "correct horse"));
	}

	@Test
	void rejectsBlankShortLongAndAdminPasswords()
	{
		CredentialStore store = new CredentialStore(dir);
		assertThrows(IllegalArgumentException.class, () -> store.change("  ", "correct horse"));
		assertThrows(IllegalArgumentException.class, () -> store.change(null, "correct horse"));
		assertThrows(IllegalArgumentException.class, () -> store.change("jordan", null));
		assertThrows(IllegalArgumentException.class, () -> store.change("jordan", "short"));
		assertThrows(IllegalArgumentException.class, () -> store.change("jordan", "x".repeat(1001)));
		assertThrows(IllegalArgumentException.class, () -> store.change("jordan", "ADMIN"));
		assertFalse(Files.exists(dir.resolve("credentials.json")));
		assertTrue(store.mustChange());
	}

	@Test
	void errorMessagesDoNotEchoPassword()
	{
		CredentialStore store = new CredentialStore(dir);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> store.change("jordan", "short"));
		assertFalse(e.getMessage().contains("short"));
	}

	@Test
	void acceptsBoundaryLengths() throws IOException
	{
		CredentialStore store = new CredentialStore(dir);
		store.change("jordan", "12345678");
		assertTrue(store.verify("jordan", "12345678"));
		store.change("jordan", "y".repeat(1000));
		assertTrue(store.verify("jordan", "y".repeat(1000)));
	}

	@Test
	void unicodeUsernameRoundTrips() throws IOException
	{
		new CredentialStore(dir).change("jörðan-日本", "pässwörd-123");
		CredentialStore reloaded = new CredentialStore(dir);
		assertTrue(reloaded.verify("jörðan-日本", "pässwörd-123"));
		assertFalse(reloaded.verify("jordan", "pässwörd-123"));
	}

	@Test
	void corruptFileNeverVerifiesAndDoesNotCrash() throws IOException
	{
		Files.writeString(dir.resolve("credentials.json"), "{not json", StandardCharsets.UTF_8);
		CredentialStore store = new CredentialStore(dir);
		assertFalse(store.verify("admin", "admin"));
		assertFalse(store.verify("jordan", "correct horse"));
	}

	@Test
	void hashesDifferPerSaveSameInput() throws IOException
	{
		CredentialStore store = new CredentialStore(dir);
		store.change("jordan", "correct horse");
		String first = Files.readString(dir.resolve("credentials.json"), StandardCharsets.UTF_8);
		store.change("jordan", "correct horse");
		String second = Files.readString(dir.resolve("credentials.json"), StandardCharsets.UTF_8);
		assertNotEquals(first, second);
	}
}
