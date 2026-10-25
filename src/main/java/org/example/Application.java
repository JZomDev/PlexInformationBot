package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import kekolab.javaplex.PlexApi;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.bot.BotController;
import org.example.bot.BotLifecycle;
import org.example.bot.BotState;
import org.example.config.AppConfig;
import org.example.config.SettingsStore;
import org.example.listeners.SetupCommandsListener;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.example.config.Settings;
import org.example.listeners.MessageListener;
import org.example.listeners.PlexListener;
import org.example.listeners.ReactListener;
import org.example.listeners.RoleListener;
import org.example.listeners.ServerBecomesAvailable;
import org.example.workers.CountPlexUsersWorker;
import org.example.workers.DownloadQueueWorker;
import org.example.workers.PlexInformationWorker;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

public class Application implements BotController
{

	private final Logger logger = LogManager.getLogger(Application.class);

	private final BotLifecycle lifecycle = new BotLifecycle(this::connect);
	private volatile SettingsStore settingsStore;
	private PlexMediaServer plexMediaServer;
	private PlexApi plexApi;
	private static final long SESSION_CACHE_MS = 2000;
	private volatile String lastEmbedSignature = "";
	private List<PlexMediatag<?>> cachedSessions;
	private long cachedSessionsAt;
	private final AtomicBoolean queueEmbedTaskRunning = new AtomicBoolean(false);

	/** Lets the setup slash commands save the message IDs they create. */
	public void setSettingsStore(SettingsStore store)
	{
		this.settingsStore = store;
	}

	private void registerSetupCommands(DiscordApi api, Server server)
	{
		api.bulkOverwriteServerApplicationCommands(server, new SlashCommandsSetUp().getSetupCommands())
			.exceptionally(e -> {
				logger.warn("Could not register setup commands for a server: {}", e.getClass().getSimpleName());
				return null;
			});
	}

	@Override
	public void restart(Settings effective)
	{
		AppConfig.reload();
		lifecycle.restart(effective);
	}

	@Override
	public BotState state()
	{
		return lifecycle.state();
	}

	/** Runs on the lifecycle thread: opens the one Discord connection and its scheduler. */
	private BotLifecycle.Connection connect(Settings settings) throws Exception
	{
		clearSessions();
		lastEmbedSignature = "";
		queueEmbedTaskRunning.set(false);
		initApi(settings);
		try
		{
			initPlexMediaServer(settings);
		}
		catch (Exception e)
		{
			plexMediaServer = null;
			logger.error("Could not reach Plex yet, will retry: {}", e.getClass().getSimpleName());
		}

		SlashCommandsSetUp slashCommandsSetUp = new SlashCommandsSetUp();

		DiscordApiBuilder builder = new DiscordApiBuilder();
		builder.setAllIntents();
		builder.setToken(settings.botToken());
		builder.setTrustAllCertificates(false);
		builder.setWaitForServersOnStartup(false);
		builder.setWaitForUsersOnStartup(false);
		builder.addServerBecomesAvailableListener(new ServerBecomesAvailable());
		builder.addServerBecomesAvailableListener((ServerBecomesAvailableEvent e) -> registerSetupCommands(e.getApi(), e.getServer()));
		builder.addServerJoinListener((ServerJoinEvent e) -> registerSetupCommands(e.getApi(), e.getServer()));
		builder.addListener(new ReactListener());
		builder.addListener(new MessageListener());
		builder.addListener(new RoleListener(this::getServer));

		DiscordApi api = builder.login().join();
		ScheduledExecutorService service;
		try
		{
			logger.info("You can invite me by using the following url: {}", api.createBotInvite());

			api.bulkOverwriteGlobalApplicationCommands(slashCommandsSetUp.getCommands());
			api.addSlashCommandCreateListener(new PlexListener(getServer(), this));
			SettingsStore store = settingsStore;
			if (store != null)
			{
				api.addSlashCommandCreateListener(new SetupCommandsListener(store));
			}
			for (Server joined : api.getServers())
			{
				registerSetupCommands(api, joined);
			}
			service = launchScheduledExecutor(api);
		}
		catch (Throwable e)
		{
			disconnect(api);
			throw e;
		}
		return () -> {
			disconnect(api);
			service.shutdownNow();
			try
			{
				service.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			clearSessions();
		};
	}

	private void disconnect(DiscordApi api)
	{
		try
		{
			api.disconnect().get(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		catch (Exception e)
		{
			logger.warn("Discord disconnect did not finish cleanly: {}", e.getClass().getSimpleName());
		}
	}

	private synchronized void clearSessions()
	{
		cachedSessions = null;
		cachedSessionsAt = 0;
	}

	private static boolean blank(String value)
	{
		return value == null || value.isBlank();
	}

	private ScheduledExecutorService launchScheduledExecutor(DiscordApi api)
	{
		ScheduledExecutorService mService = Executors.newScheduledThreadPool(2);
		CountPlexUsersWorker countPlexUsersWorker = new CountPlexUsersWorker();
		PlexInformationWorker plexInformationWorker = new PlexInformationWorker();
		DownloadQueueWorker downloadQueueWorker = new DownloadQueueWorker();

		mService.scheduleWithFixedDelay(() -> {
				// Perform your recurring method calls in here.
				try
				{
					countPlexUsersWorker.execute(api, getSessions()).whenComplete((str, err) ->
					{
						if (err == null)
						{
							api.updateActivity(str);
							LocalDateTime myObj = LocalDateTime.now();
							logger.info("Activity was modified at {}", myObj.toString());
						}
						else
						{
							logger.error("Scheduled task failed: {}", err.getClass().getSimpleName());
						}
					});
				}
				catch (Exception e)
				{
					logger.error("Scheduled task failed: {}", e.getClass().getSimpleName());
				}
			},
			0, // How long to delay the start
			15, // How long between executions
			TimeUnit.SECONDS); // The time unit used

		mService.scheduleWithFixedDelay(() -> {
				// Perform your recurring method calls in here.
				try
				{
					Settings cfg = AppConfig.get();
					if (blank(cfg.statusChannelId()) || blank(cfg.statusMessageId()))
					{
						return;
					}
					if (api.getTextChannelById(cfg.statusChannelId()).isEmpty())
					{
						return;
					}
					TextChannel textChannel = api.getTextChannelById(cfg.statusChannelId()).get();

					PlexInformationWorker.InfoEmbed info = plexInformationWorker.execute(api, getServer(), getSessions()).join();
					String signature = cfg.statusChannelId() + "/" + cfg.statusMessageId() + "/" + (info == null ? "" : info.signature());
					if (info != null && !signature.equals(lastEmbedSignature))
					{
						Message msg = api.getMessageById(cfg.statusMessageId(), textChannel).join();
						if (msg != null)
						{
							msg.edit(info.embed()).join();
							lastEmbedSignature = signature;
							logger.info("Message was modified at {}", LocalDateTime.now());
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Scheduled task failed: {}", e.getClass().getSimpleName());
				}
			},
			0, // How long to delay the start
			5, // How long between executions
			TimeUnit.SECONDS); // The time unit used


		mService.scheduleWithFixedDelay(() -> {
				if (!queueEmbedTaskRunning.compareAndSet(false, true))
				{
					logger.debug("Skipping queue message update, previous run still in progress");
					return;
				}
				try
				{
					Settings cfg = AppConfig.get();
					if (blank(cfg.queueChannelId()) || blank(cfg.queueMessageId())
						|| api.getTextChannelById(cfg.queueChannelId()).isEmpty())
					{
						queueEmbedTaskRunning.set(false);
						return;
					}
					TextChannel textChannel = api.getTextChannelById(cfg.queueChannelId()).get();
					downloadQueueWorker.execute(api, this).whenComplete(((embedBuilder, throwable) ->
					{
						try
						{
							if (throwable == null)
							{
								api.getMessageById(cfg.queueMessageId(), textChannel).whenComplete((msg, err) ->
								{
									if (err == null)
									{
										msg.edit(embedBuilder);
										LocalDateTime myObj = LocalDateTime.now();
										logger.debug("Queue message was modified at {}", myObj.toString());
									}
									else
									{
										logger.error("Scheduled task failed: {}", err.getClass().getSimpleName());
									}
									queueEmbedTaskRunning.set(false);
								});
							}
							else
							{
								logger.error("Scheduled task failed: {}", throwable.getClass().getSimpleName());
								queueEmbedTaskRunning.set(false);
							}
						}
						catch (Exception e)
						{
							queueEmbedTaskRunning.set(false);
							logger.error("Scheduled task failed: {}", e.getClass().getSimpleName());
						}
					}));
				}
				catch (Exception e)
				{
					queueEmbedTaskRunning.set(false);
					logger.error("Scheduled task failed: {}", e.getClass().getSimpleName());
				}
			},
			0, // How long to delay the start
			120, // How long between executions
			TimeUnit.SECONDS); // The time unit used
		return mService;
	}

	private PlexApi getApi()
	{
		return plexApi;
	}

	private void initApi(Settings settings)
	{
		PlexApi.Builder apiBuilder = PlexApi.Builder.withDefaultHttpClient();
		apiBuilder.withPlexDeviceName("Plex Information Bot");
		plexApi = apiBuilder.build();
		if (!settings.plexToken().isEmpty())
		{
			plexApi.withToken(settings.plexToken());
		}
	}

	/** Returns the Plex server, retrying the connection if the start-up attempt failed. */
	public synchronized PlexMediaServer getServer()
	{
		if (plexMediaServer == null && plexApi != null)
		{
			try
			{
				initPlexMediaServer(AppConfig.get());
			}
			catch (Exception e)
			{
				logger.error("Plex is not reachable: {}", e.getClass().getSimpleName());
			}
		}
		return plexMediaServer;
	}

	private void initPlexMediaServer(Settings settings) throws URISyntaxException
	{
		plexMediaServer = getApi().getMediaServer(new URI("http://" + settings.plexIp() + ":" + settings.plexPort()));
	}

	public synchronized PlexStatus getStatus()
	{
		return getServer().status();
	}

	public synchronized List<PlexMediatag<?>> getSessions()
	{
		long now = System.currentTimeMillis();
		if (now - cachedSessionsAt < SESSION_CACHE_MS)
		{
			return cachedSessions;
		}
		try
		{
			cachedSessions = getStatus().sessions();
		}
		catch (Exception e)
		{
			logger.error("Scheduled task failed: {}", e.getClass().getSimpleName());
			cachedSessions = null;
		}
		cachedSessionsAt = now;
		return cachedSessions;
	}


	private Map<String, String> getMovieTitlesById() throws IOException
	{
		Map<String, String> titlesById = new HashMap<>();
		String urlStr = "http://" + AppConfig.get().radarrUrl() + "/api/v3/movie";

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", AppConfig.get().radarrApiKey());
			connection.connect();

			if (connection.getResponseCode() >= 400)
			{
				logger.error("Radarr movies request failed with status {}", connection.getResponseCode());
				return titlesById;
			}

			try (InputStream stream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(stream))
			{
				JsonElement root = JsonParser.parseReader(reader);
				if (!root.isJsonArray())
				{
					return titlesById;
				}

				JsonArray items = root.getAsJsonArray();
				for (JsonElement item : items)
				{
					if (!item.isJsonObject())
					{
						continue;
					}

					JsonObject movie = item.getAsJsonObject();
					String movieId = getStringOrEmpty(movie, "id");
					if (movieId.isEmpty())
					{
						continue;
					}
					String title = getStringOrEmpty(movie, "title");
					String year = getStringOrEmpty(movie, "year");
					titlesById.put(movieId, year.isEmpty() ? title : title + " (" + year + ")");
				}
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return titlesById;
	}

	private Map<String, String> getSeriesTitlesById() throws IOException
	{
		Map<String, String> titlesById = new HashMap<>();
		String urlStr = "http://" + AppConfig.get().sonarrUrl() + "/api/v3/series";

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", AppConfig.get().sonarrApiKey());
			connection.connect();

			if (connection.getResponseCode() >= 400)
			{
				logger.error("Sonarr series request failed with status {}", connection.getResponseCode());
				return titlesById;
			}

			try (InputStream stream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(stream))
			{
				JsonElement root = JsonParser.parseReader(reader);
				if (!root.isJsonArray())
				{
					return titlesById;
				}

				JsonArray items = root.getAsJsonArray();
				for (JsonElement item : items)
				{
					if (!item.isJsonObject())
					{
						continue;
					}

					JsonObject series = item.getAsJsonObject();
					String seriesId = getStringOrEmpty(series, "id");
					if (!seriesId.isEmpty())
					{
						titlesById.put(seriesId, getStringOrEmpty(series, "title"));
					}
				}
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return titlesById;
	}

	public List<DownloadDetails> getQueueDetailsRadarr() throws IOException
	{
		List<DownloadDetails> queueDetails = new ArrayList<>();
		String urlStr = "http://" + AppConfig.get().radarrUrl() + "/api/v3/queue/details";
		Map<String, String> movieTitlesById = getMovieTitlesById();

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", AppConfig.get().radarrApiKey());
			connection.connect();

			if (connection.getResponseCode() >= 400)
			{
				logger.error("Radarr queue request failed with status {}", connection.getResponseCode());
				return queueDetails;
			}

			try (InputStream stream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(stream))
			{
				JsonElement root = JsonParser.parseReader(reader);
				if (!root.isJsonArray())
				{
					return queueDetails;
				}

				JsonArray items = root.getAsJsonArray();
				for (JsonElement item : items)
				{
					if (!item.isJsonObject())
					{
						continue;
					}

					JsonObject queueItem = item.getAsJsonObject();
					String movieId = getStringOrEmpty(queueItem, "movieId");
					queueDetails.add(new DownloadDetails(
						getStringOrEmpty(queueItem, "status"),
						movieTitlesById.getOrDefault(movieId, "unknown"),
						getLongOrZero(queueItem, "size"),
						getStringOrEmpty(queueItem, "added"),
						toHammertime(getStringOrEmpty(queueItem, "estimatedCompletionTime"))));
				}
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return queueDetails;
	}

	public List<DownloadDetails> getQueueDtailsSonarr() throws IOException
	{
		List<DownloadDetails> queueDetails = new ArrayList<>();
		String urlStr = "http://" + AppConfig.get().sonarrUrl() + "/api/v3/queue/details";
		Map<String, String> seriesTitlesById = getSeriesTitlesById();

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", AppConfig.get().sonarrApiKey());
			connection.connect();

			if (connection.getResponseCode() >= 400)
			{
				logger.error("Radarr queue request failed with status {}", connection.getResponseCode());
				return queueDetails;
			}

			try (InputStream stream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(stream))
			{
				JsonElement root = JsonParser.parseReader(reader);
				if (!root.isJsonArray())
				{
					return queueDetails;
				}

				JsonArray items = root.getAsJsonArray();
				for (JsonElement item : items)
				{
					if (!item.isJsonObject())
					{
						continue;
					}

					JsonObject queueItem = item.getAsJsonObject();
					String seriesId = getStringOrEmpty(queueItem, "seriesId");
					queueDetails.add(new DownloadDetails(
						getStringOrEmpty(queueItem, "status"),
						seriesTitlesById.getOrDefault(seriesId, "unknown"),
						getLongOrZero(queueItem, "size"),
						getStringOrEmpty(queueItem, "added"),
						toHammertime(getStringOrEmpty(queueItem, "estimatedCompletionTime"))));
				}
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return queueDetails;
	}

	private String getStringOrEmpty(JsonObject obj, String key)
	{
		if (!obj.has(key) || obj.get(key).isJsonNull())
		{
			return "";
		}
		return obj.get(key).getAsString();
	}

	private long getLongOrZero(JsonObject obj, String key)
	{
		if (!obj.has(key) || obj.get(key).isJsonNull())
		{
			return 0L;
		}
		return obj.get(key).getAsLong();
	}

	public static class DownloadDetails
	{
		private final String status;
		private final String outputPath;
		private final long size;
		private final String added;
		private final String estimatedCompletionTime;

		public DownloadDetails(String status, String outputPath, long size, String added, String estimatedCompletionTime)
		{
			this.status = status;
			this.outputPath = outputPath;
			this.size = size;
			this.added = added;
			this.estimatedCompletionTime = estimatedCompletionTime;
		}

		public String getOutputPath()
		{
			return outputPath;
		}

		public String getStatus()
		{
			return status;
		}

		public long getSize()
		{
			return size;
		}

		public String getAdded()
		{
			return added;
		}

		public String getEstimatedCompletionTime()
		{
			return estimatedCompletionTime;
		}
	}

	private String toHammertime(String isoDateTime)
	{
		if (isoDateTime == null || isoDateTime.isBlank())
		{
			return "unknown";
		}

		try
		{
			long epochSeconds = Instant.parse(isoDateTime).getEpochSecond();
			long currentTime = System.currentTimeMillis() / 1000;
			if (epochSeconds < currentTime)
			{
				return "unknown";
			}
			return "<t:" + epochSeconds + ":R>";
		}
		catch (DateTimeParseException ignored)
		{
			try
			{
				long epochSeconds = LocalDateTime.parse(isoDateTime).toEpochSecond(ZoneOffset.UTC);
				return "<t:" + epochSeconds + ":R>";
			}
			catch (DateTimeParseException ex)
			{
				logger.warn("Unable to parse estimatedCompletionTime: {}", isoDateTime);
				return isoDateTime;
			}
		}
	}
}
