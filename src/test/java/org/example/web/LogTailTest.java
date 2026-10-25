package org.example.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogTailTest
{
	@TempDir
	Path tmp;

	private Path file() throws Exception
	{
		Path f = tmp.resolve("logs").resolve("bot.log");
		Files.createDirectories(f.getParent());
		return f;
	}

	@Test
	void redactsUrlTokenParams()
	{
		String out = LogRedactor.redact("GET http://p:32400/x?X-Plex-Token=abc123&type=1 and ?api_key=k9&apikey=k8 token=t7", List.of());
		assertFalse(out.contains("abc123"));
		assertFalse(out.contains("k9"));
		assertFalse(out.contains("k8"));
		assertFalse(out.contains("t7"));
		assertTrue(out.contains("type=1"));
		assertTrue(out.contains("****"));
	}

	@Test
	void redactsHeaderStyleLines()
	{
		String out = LogRedactor.redact("X-Plex-Token: secretA\nX-Api-Key: secretB\nAuthorization: Bot secretC\n"
				+ "{\"token\":\"secretD\"}", List.of());
		for (String s : List.of("secretA", "secretB", "secretC", "secretD"))
		{
			assertFalse(out.contains(s), s);
		}
	}

	@Test
	void redactsLiteralSecretsAnywhere()
	{
		String out = LogRedactor.redact("connect with hunter2hunter2 now; again hunter2hunter2", List.of("hunter2hunter2", ""));
		assertEquals("connect with **** now; again ****", out);
	}

	@Test
	void readsNewTextOnlyFromOffset() throws Exception
	{
		Path f = file();
		Files.writeString(f, "one\ntwo\n");
		LogTail.Chunk a = LogTail.read(f, -1, List.of());
		assertTrue(a.reset());
		assertEquals("one\ntwo\n", a.text());
		assertEquals(8, a.nextOffset());
		Files.writeString(f, "three\n", java.nio.file.StandardOpenOption.APPEND);
		LogTail.Chunk b = LogTail.read(f, a.nextOffset(), List.of());
		assertFalse(b.reset());
		assertEquals("three\n", b.text());
	}

	@Test
	void holdsBackTrailingPartialLine() throws Exception
	{
		Path f = file();
		Files.writeString(f, "done\nSECRETVAL-par");
		LogTail.Chunk a = LogTail.read(f, 0, List.of("SECRETVAL-partial"));
		assertEquals("done\n", a.text());
		assertEquals(5, a.nextOffset());
		Files.writeString(f, "tial end\n", java.nio.file.StandardOpenOption.APPEND);
		LogTail.Chunk b = LogTail.read(f, a.nextOffset(), List.of("SECRETVAL-partial"));
		assertEquals("**** end\n", b.text());
	}

	@Test
	void offsetBeyondLengthResetsToTail() throws Exception
	{
		Path f = file();
		Files.writeString(f, "fresh\n");
		LogTail.Chunk a = LogTail.read(f, 9999, List.of());
		assertTrue(a.reset());
		assertEquals("fresh\n", a.text());
		assertEquals(6, a.nextOffset());
	}

	@Test
	void capsResponseAndStartsOnLineBoundary() throws Exception
	{
		Path f = file();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5000; i++)
		{
			sb.append("line number ").append(i).append('\n');
		}
		Files.write(f, sb.toString().getBytes(StandardCharsets.UTF_8));
		LogTail.Chunk a = LogTail.read(f, -1, List.of());
		assertTrue(a.reset());
		assertTrue(a.text().length() <= LogTail.MAX_BYTES);
		assertTrue(a.text().startsWith("line number "));
		assertTrue(a.text().endsWith("line number 4999\n"));
		LogTail.Chunk b = LogTail.read(f, 0, List.of());
		assertFalse(b.reset());
		assertTrue(b.text().length() <= LogTail.MAX_BYTES);
		assertTrue(b.nextOffset() < Files.size(f));
	}

	@Test
	void missingFileGivesEmpty() throws Exception
	{
		LogTail.Chunk a = LogTail.read(tmp.resolve("nope.log"), 5, List.of());
		assertEquals("", a.text());
		assertEquals(0, a.nextOffset());
		assertTrue(a.reset());
	}
}
