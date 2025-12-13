package org.plexinfobot;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main
{
	private static final Logger logger = LogManager.getLogger(Main.class);

	public static final String DISCORD_MESSAGE = "React to this message to get your roles!";
	private static final String CURRENT_VERSION = "3.1.1";
	public static String DISCORD_TOKEN = "";
	public static String IP = "";
	public static String PORT = "";
	public static String PLEX_KEY = "";
	public static String ROLE_ID = "";
	public static String TEXT_CHANNELID = "";
	public static String MESSAGEID = "";
	public static String API_KEY = "";
	public static String TAUTULLI_URL = "";

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
			if (envName.equals("TAUTULLI_URL"))
			{
				TAUTULLI_URL = env_var.get(envName);
			}
		}
	}

	/**
	 * The entrance point of our program.
	 *
	 * @param args The arguments for the program. The first element should be the bot's token.
	 */
	public static void main(String[] args)
	{
		if (DISCORD_TOKEN.isEmpty())
		{
			logger.error("Failed to start Discord bot. No Discord token supplied");
			return;
		}

		logger.info("The current version of the project is {}", CURRENT_VERSION);

		Application application = new Application();
		application.start();

	}
}