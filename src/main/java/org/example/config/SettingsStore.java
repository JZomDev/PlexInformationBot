package org.example.config;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class SettingsStore
{
	private static final String FILE_NAME = "settings.json";
	private static final Gson GSON = new Gson();

	private final Path dir;
	private final Settings envSettings;
	private volatile Settings fileValues;
	private final String loadWarning;
	private volatile Settings current;

	public SettingsStore(Path dir, Map<String, String> env)
	{
		this.dir = dir;
		this.envSettings = Settings.fromEnv(env);
		Settings loaded = Settings.empty();
		String warning = null;
		Path file = dir.resolve(FILE_NAME);
		if (Files.exists(file))
		{
			try
			{
				Settings parsed = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Settings.class);
				if (parsed == null)
				{
					warning = "settings.json is empty; using environment variables.";
				}
				else
				{
					loaded = parsed;
				}
			}
			catch (Exception e)
			{
				warning = "settings.json could not be read; using environment variables.";
			}
		}
		this.fileValues = loaded;
		this.loadWarning = warning;
		this.current = envSettings.overlay(loaded);
	}

	public Settings effective()
	{
		return current;
	}

	public Settings fileValues()
	{
		return fileValues;
	}

	public Optional<String> loadWarning()
	{
		return Optional.ofNullable(loadWarning);
	}

	public synchronized void save(Settings values) throws IOException
	{
		Files.createDirectories(dir);
		Path tmp = dir.resolve(FILE_NAME + ".tmp");
		Files.writeString(tmp, GSON.toJson(values), StandardCharsets.UTF_8);
		Files.move(tmp, dir.resolve(FILE_NAME), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		this.fileValues = values;
		this.current = envSettings.overlay(values);
	}

	/** Reads, changes and writes the file values under one lock; state changes only after a successful write. */
	public synchronized Settings update(UnaryOperator<Settings> change) throws IOException
	{
		Settings next = change.apply(fileValues);
		save(next);
		return next;
	}
}
