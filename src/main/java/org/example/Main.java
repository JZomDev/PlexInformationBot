package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import kekolab.javaplex.PlexAccount;
import kekolab.javaplex.PlexHTTPClient;
import kekolab.javaplex.PlexHTTPClientBuilder;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.listeners.MessageListener;
import org.example.listeners.PlexListener;
import org.example.listeners.ReactListener;
import org.example.listeners.ServerBecomesAvailable;
import org.example.workers.CountPlexUsersWorker;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.w3c.dom.Document;

public class Main
{

	public static final String DISCORD_MESSAGE = "React to this message to get your roles!";
	private static final String CURRENT_VERSION = "1.0.0";
	private static final Logger logger = LogManager.getLogger(Main.class);
	public static String DISCORD_TOKEN = "";
	public static String IP = "";
	public static String PORT = "";
	public static String PLEX_KEY = "";
	public static String ROLE_ID = "";

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
		}
	}

	/**
	 * The entrance point of our program.
	 *
	 * @param args The arguments for the program. The first element should be the bot's token.
	 */
	public static void main(String[] args) throws Exception
	{
		if (DISCORD_TOKEN.equals(""))
		{
			logger.error("Failed to start Discord bot. No Discord token supplied");
			return;
		}

		DiscordApiBuilder builder = new DiscordApiBuilder();
		builder.setAllIntents();
		builder.setToken(DISCORD_TOKEN);
		builder.setTrustAllCertificates(false);
		builder.setWaitForServersOnStartup(false);
		builder.setWaitForUsersOnStartup(false);
		builder.addServerBecomesAvailableListener(new ServerBecomesAvailable());
		builder.addListener(new ReactListener(ROLE_ID));
		builder.addListener(new MessageListener());
		SlashCommandsSetUp slashCommandsSetUp = new SlashCommandsSetUp();

		logger.info("The current version of the project is " + CURRENT_VERSION);

		discordApi = builder.login().join();

		logger.info("You can invite me by using the following url: " + discordApi.createBotInvite());

		PlexHTTPClient plexHTTPClient = getPlexHTTPClient();
		PlexMediaServer plexMediaServer = getPlexMediaServer(plexHTTPClient);
//		PlexAccount account = getPlexAccount(plexHTTPClient);


		String userName = plexMediaServer.getMyPlexUsername();
		String machineID = plexMediaServer.getMachineIdentifier();

		URL url = new URL("https://plex.tv/api/servers/" +  plexMediaServer.getMachineIdentifier() + "/shared_servers");
		HttpURLConnection request = (HttpURLConnection ) url.openConnection();
		request.addRequestProperty("X-Plex-Token", PLEX_KEY);
		request.addRequestProperty("Content-Type", "application/json");
		request.setRequestMethod("POST");

		request.setReadTimeout(5000);
		int status = request.getResponseCode();

		if (status == 200)
		{
			BufferedReader in = new BufferedReader(
				new InputStreamReader(request.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			System.out.println(content);
			in.close();
		}

		JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

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
		mService.scheduleAtFixedRate(() -> {
				// Perform your recurring method calls in here.
				try
				{
					CountPlexUsersWorker countPlexUsersWorker = new CountPlexUsersWorker();
					api.updateActivity(countPlexUsersWorker.execute(api, plexMediaServer).join());
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

	private static PlexAccount getPlexAccount(PlexHTTPClient client)
	{
		PlexAccount plexAccount = new PlexAccount(client, PLEX_KEY);

		return plexAccount;
	}
}
