package org.example.announce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.config.OwnerOnlyFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Remembers which announcement types have been baselined and which keys were already announced. */
public final class AnnouncedStore
{
	private static final Gson GSON = new GsonBuilder().create();

	private final Path file;
	private Data data = new Data();
	private boolean lost;

	public AnnouncedStore(Path dir)
	{
		this.file = dir.resolve("announced.json");
		load();
	}

	private static final class Data
	{
		Set<String> baselined = new LinkedHashSet<>();
		Map<String, Set<String>> seen = new HashMap<>();
	}

	private void load()
	{
		if (!Files.exists(file))
		{
			return;
		}
		try
		{
			Data loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Data.class);
			if (loaded == null)
			{
				lost = true;
				return;
			}
			if (loaded.baselined == null)
			{
				loaded.baselined = new LinkedHashSet<>();
			}
			if (loaded.seen == null)
			{
				loaded.seen = new HashMap<>();
			}
			data = loaded;
		}
		catch (IOException | RuntimeException e)
		{
			data = new Data();
			lost = true;
		}
	}

	public synchronized boolean wasLost()
	{
		return lost;
	}

	public synchronized boolean baselined(String type)
	{
		return data.baselined.contains(type);
	}

	public synchronized void markBaselined(String type)
	{
		if (data.baselined.add(type))
		{
			save();
		}
	}

	public synchronized boolean seen(String type, String key)
	{
		Set<String> keys = data.seen.get(type);
		return keys != null && keys.contains(key);
	}

	public synchronized void markSeen(String type, String key)
	{
		if (data.seen.computeIfAbsent(type, t -> new LinkedHashSet<>()).add(key))
		{
			save();
		}
	}

	/** Marks many keys seen with a single save. */
	public synchronized void markSeenAll(String type, java.util.Collection<String> keys)
	{
		if (data.seen.computeIfAbsent(type, t -> new LinkedHashSet<>()).addAll(keys))
		{
			save();
		}
	}

	private void save()
	{
		try
		{
			OwnerOnlyFiles.writeAtomically(file, GSON.toJson(data));
		}
		catch (IOException e)
		{
			throw new java.io.UncheckedIOException(e);
		}
	}
}
