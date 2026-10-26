package org.example.web;

import org.example.bot.BotState;
import org.example.config.Settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Cached up/down state of each section for the setup page's dots. The checks are the same ones the Test
 * buttons run, executed on another thread, so a slow or hung service never holds up a request. Callers always
 * get the last cached value ("unknown" before the first result).
 */
final class HealthMonitor
{
	static final long BACKGROUND_INTERVAL_MS = 60_000;
	static final long ON_DEMAND_INTERVAL_MS = 10_000;
	private static final String[] CHECKED = {"plex", "tautulli", "radarr", "sonarr"};

	record Health(String state, String message)
	{
	}

	private static final Health UNKNOWN = new Health("unknown", "Checking");
	private static final Health UNSET = new Health("unset", "Not set up");

	private final Supplier<Settings> settings;
	private final ConnectionChecks checks;
	private final Supplier<BotState> bot;
	private final Executor executor;
	private final LongSupplier clock;
	private final Map<String, Health> results = new ConcurrentHashMap<>();
	private final Map<String, Long> inFlight = new ConcurrentHashMap<>();
	private final Object lock = new Object();
	private long lastStart = -1;
	private long generation;

	HealthMonitor(Supplier<Settings> settings, ConnectionChecks checks, Supplier<BotState> bot, Executor executor,
				  LongSupplier clock)
	{
		this.settings = settings;
		this.checks = checks;
		this.bot = bot;
		this.executor = executor;
		this.clock = clock;
	}

	/** The cached states. Starts a background refresh first when the last one is older than the interval. */
	Map<String, Health> snapshot(boolean onDemand)
	{
		Settings s = settings.get();
		refreshIfDue(s, onDemand ? ON_DEMAND_INTERVAL_MS : BACKGROUND_INTERVAL_MS);
		Map<String, Health> out = new LinkedHashMap<>();
		for (String section : CHECKED)
		{
			out.put(section, configured(section, s) ? results.getOrDefault(section, UNKNOWN) : UNSET);
		}
		out.put("discord", discord(s));
		return out;
	}

	/** Saved settings changed: old results no longer apply and the next request may refresh at once. */
	void settingsChanged()
	{
		synchronized (lock)
		{
			generation++;
			lastStart = -1;
			results.clear();
		}
	}

	private void refreshIfDue(Settings s, long intervalMs)
	{
		long gen;
		synchronized (lock)
		{
			long now = clock.getAsLong();
			if (lastStart >= 0 && now - lastStart < intervalMs)
			{
				return;
			}
			lastStart = now;
			gen = generation;
		}
		for (String section : CHECKED)
		{
			Long running = inFlight.get(section);
			if (!configured(section, s) || (running != null && running == gen))
			{
				continue;
			}
			inFlight.put(section, gen);
			try
			{
				executor.execute(() -> run(section, s, gen));
			}
			catch (RuntimeException e)
			{
				inFlight.remove(section, gen);
			}
		}
	}

	private void run(String section, Settings s, long gen)
	{
		Health h;
		try
		{
			CheckResult r = switch (section)
			{
				case "plex" -> checks.plex(s.plexIp(), s.plexPort(), s.plexToken());
				case "tautulli" -> checks.tautulli(s.tautulliUrl(), s.tautulliApiKey());
				case "radarr" -> checks.radarr(s.radarrUrl(), s.radarrApiKey());
				default -> checks.sonarr(s.sonarrUrl(), s.sonarrApiKey());
			};
			h = new Health(r.ok() ? "ok" : "down", masked(r.message(), s));
		}
		catch (RuntimeException e)
		{
			h = new Health("down", "Check failed");
		}
		finally
		{
			inFlight.remove(section, gen);
		}
		synchronized (lock)
		{
			if (gen == generation)
			{
				results.put(section, h);
			}
		}
	}

	private Health discord(Settings s)
	{
		if (s.botToken().isBlank())
		{
			return UNSET;
		}
		return switch (bot.get())
		{
			case RUNNING -> new Health("ok", "Bot is connected");
			case STARTING -> new Health("unknown", "Bot is starting");
			case FAILED -> new Health("down", "Bot could not log in");
			case NOT_CONFIGURED -> UNSET;
		};
	}

	private static boolean configured(String section, Settings s)
	{
		return switch (section)
		{
			case "plex" -> !(s.plexIp().isBlank() && s.plexToken().isBlank());
			case "tautulli" -> !(s.tautulliUrl().isBlank() && s.tautulliApiKey().isBlank());
			case "radarr" -> !(s.radarrUrl().isBlank() && s.radarrApiKey().isBlank());
			default -> !(s.sonarrUrl().isBlank() && s.sonarrApiKey().isBlank());
		};
	}

	/** The checks already mask their own secret; this is a second guard against every saved secret. */
	private static String masked(String message, Settings s)
	{
		String out = message == null ? "" : message;
		for (String secret : new String[] {s.botToken(), s.plexToken(), s.tautulliApiKey(), s.radarrApiKey(),
				s.sonarrApiKey()})
		{
			if (!secret.isBlank())
			{
				out = out.replace(secret, "********");
			}
		}
		return out;
	}
}
