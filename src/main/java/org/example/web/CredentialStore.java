package org.example.web;

import com.google.gson.Gson;
import org.example.config.OwnerOnlyFiles;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class CredentialStore
{
	private static final String FILE_NAME = "credentials.json";
	private static final String DEFAULT_USER = "admin";
	private static final String DEFAULT_PASSWORD = "admin";
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int ITERATIONS = 210000;
	private static final int SALT_BYTES = 16;
	private static final int KEY_BITS = 256;
	private static final int MIN_LENGTH = 8;
	private static final int MAX_LENGTH = 1000;
	private static final Gson GSON = new Gson();
	private static final SecureRandom RANDOM = new SecureRandom();

	private record Stored(String username, String salt, String hash, int iterations)
	{
	}

	private final Path dir;

	public CredentialStore(Path dir)
	{
		this.dir = dir;
	}

	public boolean mustChange()
	{
		return !Files.exists(dir.resolve(FILE_NAME));
	}

	public boolean verify(String username, String password)
	{
		if (username == null || password == null || password.length() > MAX_LENGTH)
		{
			return false;
		}
		if (mustChange())
		{
			return constantEquals(username, DEFAULT_USER) & constantEquals(password, DEFAULT_PASSWORD);
		}
		Stored stored = read();
		if (stored == null)
		{
			return false;
		}
		try
		{
			byte[] salt = Base64.getDecoder().decode(stored.salt());
			byte[] expected = Base64.getDecoder().decode(stored.hash());
			byte[] actual = hash(password, salt, stored.iterations());
			boolean nameOk = constantEquals(username, stored.username());
			return MessageDigest.isEqual(expected, actual) & nameOk;
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	public synchronized void change(String username, String password) throws IOException
	{
		if (username == null || username.isBlank())
		{
			throw new IllegalArgumentException("Username must not be blank.");
		}
		if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH)
		{
			throw new IllegalArgumentException("Password must be " + MIN_LENGTH + " to " + MAX_LENGTH + " characters.");
		}
		if (password.equalsIgnoreCase(DEFAULT_PASSWORD))
		{
			throw new IllegalArgumentException("Password must not be the default password.");
		}
		byte[] salt = new byte[SALT_BYTES];
		RANDOM.nextBytes(salt);
		byte[] hash = hash(password, salt, ITERATIONS);
		Stored stored = new Stored(username, Base64.getEncoder().encodeToString(salt),
				Base64.getEncoder().encodeToString(hash), ITERATIONS);

		OwnerOnlyFiles.writeAtomically(dir.resolve(FILE_NAME), GSON.toJson(stored));
	}

	private Stored read()
	{
		try
		{
			Stored stored = GSON.fromJson(Files.readString(dir.resolve(FILE_NAME), StandardCharsets.UTF_8), Stored.class);
			if (stored == null || stored.username() == null || stored.salt() == null || stored.hash() == null
					|| stored.iterations() < 1)
			{
				return null;
			}
			return stored;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private static byte[] hash(String password, byte[] salt, int iterations)
	{
		char[] chars = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, KEY_BITS);
		try
		{
			return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
		}
		catch (GeneralSecurityException e)
		{
			throw new IllegalStateException("Password hashing is unavailable.");
		}
		finally
		{
			spec.clearPassword();
			Arrays.fill(chars, '\0');
		}
	}

	private static boolean constantEquals(String a, String b)
	{
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}
}
