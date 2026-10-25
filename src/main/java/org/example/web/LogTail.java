package org.example.web;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/** Reads new complete lines from one fixed log file, redacted. No caller-supplied paths. */
final class LogTail
{
	static final int MAX_BYTES = 64 * 1024;

	record Chunk(String text, long nextOffset, boolean reset)
	{
	}

	private LogTail()
	{
	}

	static Chunk read(Path file, long offset, Collection<String> secrets) throws IOException
	{
		if (!Files.isRegularFile(file))
		{
			return new Chunk("", 0, offset != 0);
		}
		try (RandomAccessFile in = new RandomAccessFile(file.toFile(), "r"))
		{
			long len = in.length();
			boolean reset = offset < 0 || offset > len;
			long start = reset ? Math.max(0, len - MAX_BYTES) : offset;
			int want = (int) Math.min(MAX_BYTES, len - start);
			byte[] buf = new byte[want];
			in.seek(start);
			in.readFully(buf);
			int from = 0;
			if (reset && start > 0)
			{
				// the tail begins mid-line: drop the partial first line
				while (from < buf.length && buf[from] != '\n')
				{
					from++;
				}
				from = Math.min(buf.length, from + 1);
			}
			int to = buf.length;
			while (to > from && buf[to - 1] != '\n')
			{
				to--;
			}
			if (to == from && buf.length - from >= MAX_BYTES - 1)
			{
				// one line longer than the cap: send it rather than stall
				to = buf.length;
			}
			if (to <= from)
			{
				return new Chunk("", start + from, reset);
			}
			String text = new String(buf, from, to - from, StandardCharsets.UTF_8);
			return new Chunk(LogRedactor.redact(text, secrets), start + to, reset);
		}
	}
}
