package org.example.announce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.Settings;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.javacord.api.DiscordApi;

/** Runs the announcer on each scheduler tick, never throwing, and remembers how the last cycle went. */
public final class AnnouncementService
{
	private final Logger logger = LogManager.getLogger(AnnouncementService.class);
	private final Announcer announcer;
	private final Supplier<Settings> settings;
	private volatile Announcer.Result last;

	public AnnouncementService(Announcer announcer, Supplier<Settings> settings)
	{
		this.announcer = announcer;
		this.settings = settings;
	}

	/** Builds the service for one Discord connection; state is kept in the config directory. */
	public static AnnouncementService create(DiscordApi api, Supplier<Settings> settings, Path configDir)
	{
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		Announcer announcer = new Announcer(new HttpArrSource(settings, http), DiscordCardSink.forApi(api, settings),
				new AnnouncedStore(configDir), Clock.systemUTC());
		return new AnnouncementService(announcer, settings);
	}

	public void tick()
	{
		try
		{
			Settings s = settings.get();
			last = announcer.runCycle(s.announceMovies(), s.announceSeasons(), !s.announceChannelId().isBlank());
		}
		catch (Exception e)
		{
			logger.warn("Announcement cycle failed: {}", e.getClass().getSimpleName());
		}
	}

	/** The last completed cycle, if any. */
	public Optional<Announcer.Result> lastResult()
	{
		return Optional.ofNullable(last);
	}

	/** A short, secret-free description of what went wrong in the last cycle, if anything. */
	public Optional<String> lastProblem()
	{
		Announcer.Result r = last;
		if (r == null)
		{
			return Optional.empty();
		}
		List<String> parts = new ArrayList<>();
		if (r.moviesFailed())
		{
			parts.add("Radarr unreachable");
		}
		if (r.seasonsFailed())
		{
			parts.add("Sonarr unreachable");
		}
		if (r.sinkFailed())
		{
			parts.add("could not post to the channel");
		}
		return parts.isEmpty() ? Optional.empty() : Optional.of(String.join("; ", parts));
	}
}
