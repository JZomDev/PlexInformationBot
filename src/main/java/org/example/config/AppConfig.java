package org.example.config;

/** Holds the current effective settings. Readers call get() on every tick or event. */
public final class AppConfig
{
	private static volatile SettingsStore store;
	private static volatile Settings cached = Settings.empty();

	private AppConfig()
	{
	}

	public static synchronized void init(SettingsStore settingsStore)
	{
		store = settingsStore;
		cached = settingsStore.effective();
	}

	public static Settings get()
	{
		return cached;
	}

	public static synchronized void reload()
	{
		SettingsStore s = store;
		if (s != null)
		{
			cached = s.effective();
		}
	}
}
