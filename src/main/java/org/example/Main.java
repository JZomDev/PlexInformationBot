package org.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import kekolab.javaplex.PlexHTTPClient;
import kekolab.javaplex.PlexHTTPClientBuilder;
import kekolab.javaplex.PlexMediaServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.listeners.MessageListener;
import org.example.listeners.PlexListener;
import org.example.listeners.ReactListener;
import org.example.listeners.RoleListener;
import org.example.listeners.ServerBecomesAvailable;
import org.example.workers.CountPlexUsersWorker;
import org.example.workers.PlexInformationWorker;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;

public class Main
{

	public static final String DISCORD_MESSAGE = "React to this message to get your roles!";
	private static final String CURRENT_VERSION = "2.1.0";
	private static final Logger logger = LogManager.getLogger(Main.class);
	public static String DISCORD_TOKEN = "";
	public static String IP = "";
	public static String PORT = "";
	public static String PLEX_KEY = "";
	public static String ROLE_ID = "";
	public static String TEXT_CHANNELID = "";
	public static String MESSAGEID = "";
	public static String API_KEY = "";
	public static String VALID_EMAIL_ADDRESS_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
		+ "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	public static Pattern PATTERN_MATCH = Pattern.compile(VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
	static DiscordApi discordApi = null;
	private static ScheduledExecutorService mService;

	static
	{
		Map<String, String> env_var = System.getenv();

		for (String envName : env_var.keySet())
		{
			if (envName.equals("BOT_TOKEN"))
			{
				DISCORD_TOKEN = env_var.get(envName);
			}
			if (envName.equals("IP"))
			{
				IP = env_var.get(envName);
			}
			if (envName.equals("PORT"))
			{
				PORT = env_var.get(envName);
			}
			if (envName.equals("PLEX_KEY"))
			{
				PLEX_KEY = env_var.get(envName);
			}
			if (envName.equals("ROLE_ID"))
			{
				ROLE_ID = env_var.get(envName);
			}
			if (envName.equals("TEXT_CHANNELID"))
			{
				TEXT_CHANNELID = env_var.get(envName);
			}
			if (envName.equals("MESSAGEID"))
			{
				MESSAGEID = env_var.get(envName);
			}
			if (envName.equals("API_KEY"))
			{
				API_KEY = env_var.get(envName);
			}
		}
	}

	/**
	 * The entrance point of our program.
	 *
	 * @param args The arguments for the program. The first element should be the bot's token.
	 */
	public static void main(String[] args) throws Exception
	{
		if (DISCORD_TOKEN.isEmpty())
		{
			logger.error("Failed to start Discord bot. No Discord token supplied");
			return;
		}

		logger.info("The current version of the project is {}", CURRENT_VERSION);

		PlexHTTPClient plexHTTPClient = getPlexHTTPClient();
		PlexMediaServer plexMediaServer = getPlexMediaServer(plexHTTPClient);
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
		builder.addListener(new RoleListener(plexMediaServer));

		discordApi = builder.login().join();

		logger.info("You can invite me by using the following url: {}", discordApi.createBotInvite());

		discordApi.bulkOverwriteGlobalApplicationCommands(slashCommandsSetUp.getCommands());
		discordApi.addSlashCommandCreateListener(new PlexListener(plexMediaServer));

		launchScheduledExecutor(discordApi, plexMediaServer);
	}

	public static void launchScheduledExecutor(DiscordApi api, PlexMediaServer plexMediaServer)
	{
		if (mService == null || mService.isShutdown())
		{
			mService = Executors.newScheduledThreadPool(1);
		}
		AtomicInteger i = new AtomicInteger();
		mService.scheduleAtFixedRate(() -> {
				// Perform your recurring method calls in here.
				try
				{
					PlexInformationWorker plexInformationWorker = new PlexInformationWorker();
					CountPlexUsersWorker countPlexUsersWorker = new CountPlexUsersWorker();
					TextChannel textChannel = api.getTextChannelById(TEXT_CHANNELID).get();

					plexInformationWorker.execute(api).whenComplete(((embedBuilder, throwable) ->
					{
						api.getMessageById(MESSAGEID, textChannel).whenComplete((msg, err) ->
						{
							if (err == null)
							{
								msg.edit(embedBuilder).whenComplete((m, e) ->
								{
									LocalDateTime myObj = LocalDateTime.now();
									i.getAndIncrement();
									logger.info("Message was modified at {} for the {} time", myObj.toString(), i.get());
								});
							}
							else
							{
								logger.error(err.getMessage(), err);
							}
						});
					}));

					countPlexUsersWorker.execute(api, plexMediaServer).whenComplete((str, err) ->
					{
						if (err == null)
						{
							api.updateActivity(str);
						}
						else
						{
							logger.error(err.getMessage(), err);
						}
					});
				}
				catch (Exception e)
				{
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
			5, // How long between executions
			TimeUnit.SECONDS); // The time unit used
	}

	public static CompletableFuture<Boolean> finishExecutor() throws InterruptedException
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

	public static void awaitTerminationAfterShutdown(ExecutorService threadPool)
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

	private static PlexHTTPClient getPlexHTTPClient()
	{
		PlexHTTPClient plexPlexHTTPClient = new PlexHTTPClientBuilder()
			.withPlexDeviceName("Plex Information Bot")
			.build();

		return plexPlexHTTPClient;
	}

	private static PlexMediaServer getPlexMediaServer(PlexHTTPClient client) throws URISyntaxException
	{
		PlexMediaServer plexMediaServer = new PlexMediaServer(new URI("http://" + IP + ":" + PORT), client, PLEX_KEY);

		return plexMediaServer;
	}
}
