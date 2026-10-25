package org.example;

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
	public static String QUEUE_TEXT_CHANNELID = "";
	public static String MESSAGEID = "";
	public static String QUEUEMESSAGEID = "";
	public static String API_KEY = "";
	public static String TAUTULLI_URL = "";
	public static String SONARR_URL = "";
	public static String SONARR_API = "";
	public static String RADARR_URL = "";
	public static String RADARR_API = "";

	static
	{
		String discord_tokenEnv = System.getenv("BOT_TOKEN");
		if (discord_tokenEnv != null)
		{
			DISCORD_TOKEN = discord_tokenEnv;
		}
		String ipEnv = System.getenv("IP");
		if (ipEnv != null)
		{
			IP = ipEnv;
		}
		String portEnv = System.getenv("PORT");
		if (portEnv != null)
		{
			PORT = portEnv;
		}
		String plex_keyEnv = System.getenv("PLEX_KEY");
		if (plex_keyEnv != null)
		{
			PLEX_KEY = plex_keyEnv;
		}
		String role_idEnv = System.getenv("ROLE_ID");
		if (role_idEnv != null)
		{
			ROLE_ID = role_idEnv;
		}
		String text_channelidEnv = System.getenv("TEXT_CHANNELID");
		if (text_channelidEnv != null)
		{
			TEXT_CHANNELID = text_channelidEnv;
		}
		String queue_text_channelidEnv = System.getenv("QUEUE_TEXT_CHANNELID");
		if (queue_text_channelidEnv != null)
		{
			QUEUE_TEXT_CHANNELID = queue_text_channelidEnv;
		}
		String messageidEnv = System.getenv("MESSAGEID");
		if (messageidEnv != null)
		{
			MESSAGEID = messageidEnv;
		}
		String queuemessageidEnv = System.getenv("QUEUEMESSAGEID");
		if (queuemessageidEnv != null)
		{
			QUEUEMESSAGEID = queuemessageidEnv;
		}
		String api_keyEnv = System.getenv("API_KEY");
		if (api_keyEnv != null)
		{
			API_KEY = api_keyEnv;
		}
		String tautulli_urlEnv = System.getenv("TAUTULLI_URL");
		if (tautulli_urlEnv != null)
		{
			TAUTULLI_URL = tautulli_urlEnv;
		}
		String sonarr_urlEnv = System.getenv("SONARR_URL");
		if (sonarr_urlEnv != null)
		{
			SONARR_URL = sonarr_urlEnv;
		}
		String sonarr_apiEnv = System.getenv("SONARR_API");
		if (sonarr_apiEnv != null)
		{
			SONARR_API = sonarr_apiEnv;
		}
		String radarr_urlEnv = System.getenv("RADARR_URL");
		if (radarr_urlEnv != null)
		{
			RADARR_URL = radarr_urlEnv;
		}
		String radarr_apiEnv = System.getenv("RADARR_API");
		if (radarr_apiEnv != null)
		{
			RADARR_API = radarr_apiEnv;
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