package org.plexinfobot;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
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
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import static org.plexinfobot.Main.*;
import org.plexinfobot.listeners.MessageListener;
import org.plexinfobot.listeners.PlexListener;
import org.plexinfobot.listeners.ReactListener;
import org.plexinfobot.listeners.RoleListener;
import org.plexinfobot.listeners.ServerBecomesAvailable;
import org.plexinfobot.workers.CountPlexUsersWorker;
import org.plexinfobot.workers.DownloadQueueWorker;
import org.plexinfobot.workers.PlexFriendlyName;
import org.plexinfobot.workers.PlexInformationWorker;

public class Application
{
	private final Logger logger = LogManager.getLogger(Application.class);

	DiscordApi discordApi = null;
	private ScheduledExecutorService mService;
	private PlexMediaServer plexMediaServer;
	private PlexApi plexApi;
	private HashMap<String, String> friendllyUserNames;
	private final AtomicBoolean activityTaskRunning = new AtomicBoolean(false);
	private final AtomicBoolean statusEmbedTaskRunning = new AtomicBoolean(false);
	private final AtomicBoolean queueEmbedTaskRunning = new AtomicBoolean(false);

	public Application()
	{
		initApi();
		try
		{
			initPlexMediaServer();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return;
		}

		SlashCommandsSetUp slashCommandsSetUp = new SlashCommandsSetUp();

		DiscordApiBuilder builder = new DiscordApiBuilder();
		builder.setAllIntents();
		builder.setToken(DISCORD_TOKEN);
		builder.setTrustAllCertificates(false);
		builder.setWaitForServersOnStartup(false);
		builder.setWaitForUsersOnStartup(false);
		builder.addServerBecomesAvailableListener(new ServerBecomesAvailable());
		builder.addListener(new ReactListener(ROLE_ID));
		builder.addListener(new MessageListener());
		builder.addListener(new RoleListener(getServer()));

		discordApi = builder.login().join();
		logger.info("You can invite me by using the following url: {}", discordApi.createBotInvite());

		discordApi.bulkOverwriteGlobalApplicationCommands(slashCommandsSetUp.getCommands());
		discordApi.addSlashCommandCreateListener(new PlexListener(getServer(), this));
	}

	public void start()
	{
		launchScheduledExecutor(discordApi);
	}

	public void launchScheduledExecutor(DiscordApi api)
	{
		if (mService == null || mService.isShutdown())
		{
			mService = Executors.newScheduledThreadPool(2);
		}
		CountPlexUsersWorker countPlexUsersWorker = new CountPlexUsersWorker();
		PlexInformationWorker plexInformationWorker = new PlexInformationWorker(new PlexFriendlyName());
		DownloadQueueWorker downloadQueueWorker = new DownloadQueueWorker();

		mService.scheduleWithFixedDelay(() -> {
				if (!activityTaskRunning.compareAndSet(false, true))
				{
					logger.debug("Skipping activity update, previous run still in progress");
					return;
				}
				try
				{
					countPlexUsersWorker.execute(api, getSessions()).whenComplete((str, err) ->
					{
						try
						{
							if (err == null)
							{
								api.updateActivity(str);
								LocalDateTime myObj = LocalDateTime.now();
								logger.debug("Activity was modified at {}", myObj.toString());
							}
							else
							{
								logger.error(err.getMessage(), err);
							}
						}
						finally
						{
							activityTaskRunning.set(false);
						}
					});
				}
				catch (Exception e)
				{
					activityTaskRunning.set(false);
					try
					{
						finishExecutor().join();
					}
					catch (Exception e2)
					{
						throw new RuntimeException(e2);
					}
					logger.error(e.getMessage(), e);
				}
			},
			0, // How long to delay the start
			15, // How long between executions
			TimeUnit.SECONDS); // The time unit used

		mService.scheduleWithFixedDelay(() -> {
				if (!statusEmbedTaskRunning.compareAndSet(false, true))
				{
					logger.debug("Skipping status message update, previous run still in progress");
					return;
				}
				try
				{
					if (api.getTextChannelById(TEXT_CHANNELID).isEmpty())
					{
						statusEmbedTaskRunning.set(false);
						return;
					}
					TextChannel textChannel = api.getTextChannelById(TEXT_CHANNELID).get();

					plexInformationWorker.execute(api, getServer(), getSessions()).whenComplete(((embedBuilder, throwable) ->
					{
						try
						{
							if (throwable == null)
							{
								api.getMessageById(MESSAGEID, textChannel).whenComplete((msg, err) ->
								{
									if (err == null)
									{
										msg.edit(embedBuilder);
										LocalDateTime myObj = LocalDateTime.now();
										logger.debug("Message was modified at {}", myObj.toString());
									}
									else
									{
										logger.error(err.getMessage(), err);
									}
									statusEmbedTaskRunning.set(false);
								});
							}
							else
							{
								logger.error(throwable.getMessage(), throwable);
								statusEmbedTaskRunning.set(false);
							}
						}
						catch (Exception e)
						{
							statusEmbedTaskRunning.set(false);
							logger.error(e.getMessage(), e);
						}
					}));
				}
				catch (Exception e)
				{
					statusEmbedTaskRunning.set(false);
					try
					{
						finishExecutor().join();
					}
					catch (Exception e2)
					{
						throw new RuntimeException(e2);
					}
					logger.error(e.getMessage(), e);
				}
			},
			0, // How long to delay the start
			15, // How long between executions
			TimeUnit.SECONDS); // The time unit used


		mService.scheduleWithFixedDelay(() -> {
				if (!queueEmbedTaskRunning.compareAndSet(false, true))
				{
					logger.debug("Skipping queue message update, previous run still in progress");
					return;
				}
				try
				{
					if (api.getTextChannelById(QUEUE_TEXT_CHANNELID).isEmpty())
					{
						queueEmbedTaskRunning.set(false);
						return;
					}
					TextChannel textChannel = api.getTextChannelById(QUEUE_TEXT_CHANNELID).get();
					downloadQueueWorker.execute(api, this).whenComplete(((embedBuilder, throwable) ->
					{
						try
						{
							if (throwable == null)
							{
								api.getMessageById(QUEUEMESSAGEID, textChannel).whenComplete((msg, err) ->
								{
									if (err == null)
									{
										msg.edit(embedBuilder);
										LocalDateTime myObj = LocalDateTime.now();
										logger.debug("Queue message was modified at {}", myObj.toString());
									}
									else
									{
										logger.error(err.getMessage(), err);
									}
									queueEmbedTaskRunning.set(false);
								});
							}
							else
							{
								logger.error(throwable.getMessage(), throwable);
								queueEmbedTaskRunning.set(false);
							}
						}
						catch (Exception e)
						{
							queueEmbedTaskRunning.set(false);
							logger.error(e.getMessage(), e);
						}
					}));
				}
				catch (Exception e)
				{
					queueEmbedTaskRunning.set(false);
					try
					{
						finishExecutor().join();
					}
					catch (Exception e2)
					{
						throw new RuntimeException(e2);
					}
					logger.error(e.getMessage(), e);
				}
			},
			0, // How long to delay the start
			120, // How long between executions
			TimeUnit.SECONDS); // The time unit used
	}

	public CompletableFuture<Boolean> finishExecutor()
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				if (mService != null)
				{
					awaitTerminationAfterShutdown(mService);
					return true;
				}
				return false;
			}
			catch (Throwable t)
			{
				throw new CompletionException(t);
			}
		}, discordApi.getThreadPool().getExecutorService());
	}

	public void awaitTerminationAfterShutdown(ExecutorService threadPool)
	{
		threadPool.shutdown();
		try
		{
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS))
			{
				threadPool.shutdownNow();
			}
		}
		catch (InterruptedException ex)
		{
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private PlexApi getApi()
	{
		return plexApi;
	}

	private void initApi()
	{
		PlexApi.Builder apiBuilder = PlexApi.Builder.withDefaultHttpClient();
		apiBuilder.withPlexDeviceName("Plex Information Bot");
		plexApi = apiBuilder.build();
		friendllyUserNames = new HashMap<>();
		if (!PLEX_KEY.isEmpty())
		{
			plexApi.withToken(PLEX_KEY);
		}
	}

	public HashMap<String, String> getFriendlyUserNames()
	{
		return friendllyUserNames;
	}

	public PlexMediaServer getServer()
	{
		return plexMediaServer;
	}

	private void initPlexMediaServer() throws URISyntaxException
	{
		plexMediaServer = getApi().getMediaServer(new URI("http://" + IP + ":" + PORT));
	}

	public synchronized PlexStatus getStatus()
	{
		return getServer().status();
	}

	public synchronized List<PlexMediatag<?>> getSessions()
	{
		try
		{
			return getStatus().sessions();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private String getMovieName(String id) throws IOException
	{
		Map<String, String> movieTitles = getMovieTitlesById();
		return movieTitles.getOrDefault(id, "unknown");
	}

	private String getTVShowName(String id) throws IOException
	{
		Map<String, String> seriesTitles = getSeriesTitlesById();
		return seriesTitles.getOrDefault(id, "unknown");
	}

	private Map<String, String> getMovieTitlesById() throws IOException
	{
		Map<String, String> titlesById = new HashMap<>();
		String urlStr = "http://" + RADARR_URL + "/api/v3/movie";

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", RADARR_API);
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
		String urlStr = "http://" + SONARR_URL + "/api/v3/series";

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", SONARR_API);
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
		String urlStr = "http://" + RADARR_URL + "/api/v3/queue/details";
		Map<String, String> movieTitlesById = getMovieTitlesById();

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", RADARR_API);
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
		String urlStr = "http://" + SONARR_URL + "/api/v3/queue/details";
		Map<String, String> seriesTitlesById = getSeriesTitlesById();

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("X-API-Key", SONARR_API);
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
