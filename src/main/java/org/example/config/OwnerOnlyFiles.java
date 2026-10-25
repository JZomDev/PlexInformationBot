package org.example.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/** Writes files that hold secrets: owner-only where POSIX is supported, atomic, no leftover temp file. */
public final class OwnerOnlyFiles
{
	private OwnerOnlyFiles()
	{
	}

	public static void writeAtomically(Path target, String content) throws IOException
	{
		Path dir = target.toAbsolutePath().getParent();
		Files.createDirectories(dir);
		Path tmp = dir.resolve(target.getFileName() + ".tmp");
		try
		{
			Files.deleteIfExists(tmp);
			createOwnerOnly(tmp);
			Files.writeString(tmp, content, StandardCharsets.UTF_8);
			Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException | RuntimeException e)
		{
			try
			{
				Files.deleteIfExists(tmp);
			}
			catch (IOException ignored)
			{
			}
			throw e;
		}
	}

	private static void createOwnerOnly(Path file) throws IOException
	{
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
		try
		{
			Files.createFile(file, PosixFilePermissions.asFileAttribute(perms));
		}
		catch (UnsupportedOperationException e)
		{
			Files.createFile(file);
		}
	}
}
