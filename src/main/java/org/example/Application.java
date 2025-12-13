package org.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import kekolab.javaplex.PlexApi;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.example.Main.DISCORD_TOKEN;
import static org.example.Main.IP;
import static org.example.Main.MESSAGEID;
import static org.example.Main.PLEX_KEY;
import static org.example.Main.PORT;
import static org.example.Main.ROLE_ID;
import static org.example.Main.TEXT_CHANNELID;
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
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class Application
{

	private final Logger logger = LogManager.getLogger(Application.class);

	DiscordApi discordApi = null;
	private ScheduledExecutorService mService;
	private PlexMediaServer plexMediaServer;
	private PlexApi plexApi;


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
		PlexInformationWorker plexInformationWorker = new PlexInformationWorker();

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
			15, // How long between executions
			TimeUnit.SECONDS); // The time unit used

		mService.scheduleWithFixedDelay(() -> {
				// Perform your recurring method calls in here.
				try
				{
					if (api.getTextChannelById(TEXT_CHANNELID).isEmpty())
					{
						return;
					}
					TextChannel textChannel = api.getTextChannelById(TEXT_CHANNELID).get();

					EmbedBuilder embedBuilder = plexInformationWorker.execute(api, getServer(), getSessions()).join();
					if (embedBuilder != null)
					{
						Message msg = api.getMessageById(MESSAGEID, textChannel).join();
						if (msg != null)
						{
							msg.edit(embedBuilder).join();
							logger.info("Message was modified at {}", LocalDateTime.now());
						}
					}
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
			500, // How long between executions
			TimeUnit.MILLISECONDS); // The time unit used
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
		if (!PLEX_KEY.isEmpty())
		{
			plexApi.withToken(PLEX_KEY);
		}
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
			return null;
		}
	}
}

