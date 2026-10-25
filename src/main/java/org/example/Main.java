package org.example;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.AppConfig;
import org.example.config.Settings;
import org.example.config.SettingsStore;
import org.example.web.ConnectionChecks;
import org.example.web.CredentialStore;
import org.example.web.JavacordDiscordProbe;
import org.example.web.LoginThrottle;
import org.example.web.SessionManager;
import org.example.web.WebServer;

public class Main
{
	private static final Logger logger = LogManager.getLogger(Main.class);

	public static final String DISCORD_MESSAGE = "React to this message to get your roles!";
	private static final String CURRENT_VERSION = "3.1.1";
	private static final int DEFAULT_WEB_PORT = 4546;
	private static final String DEFAULT_CONFIG_DIR = "/config";

	/**
	 * The entrance point of our program.
	 *
	 * @param args Unused. Configuration comes from the setup page or environment variables.
	 */
	public static void main(String[] args)
	{
		logger.info("The current version of the project is {}", CURRENT_VERSION);

		int port = DEFAULT_WEB_PORT;
		String portEnv = System.getenv("WEB_PORT");
		if (portEnv != null && !portEnv.isBlank())
		{
			try
			{
				port = Integer.parseInt(portEnv.trim());
			}
			catch (NumberFormatException e)
			{
				logger.error("WEB_PORT is not a number, using {}", DEFAULT_WEB_PORT);
			}
		}
		String dirEnv = System.getenv("CONFIG_DIR");
		Path configDir = Path.of(dirEnv == null || dirEnv.isBlank() ? DEFAULT_CONFIG_DIR : dirEnv.trim());

		SettingsStore settings = new SettingsStore(configDir, System.getenv());
		settings.loadWarning().ifPresent(logger::warn);
		AppConfig.init(settings);

		HttpClient http = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NEVER)
			.connectTimeout(Duration.ofSeconds(5))
			.build();
		Application application = new Application();
		application.setSettingsStore(settings);
		WebServer web = new WebServer(port, settings, new CredentialStore(configDir),
			new SessionManager(Clock.systemUTC()), new LoginThrottle(Clock.systemUTC()),
			new ConnectionChecks(http, new JavacordDiscordProbe()), application);
		try
		{
			web.start();
		}
		catch (Exception e)
		{
			logger.error("Could not start the setup page on port {}: {}", port, e.getMessage());
			System.exit(1);
			return;
		}
		logger.info("Setup page is listening on port {}", port);

		Settings effective = settings.effective();
		if (effective.botToken().isEmpty())
		{
			logger.info("No Discord token yet. Open the setup page to configure the bot.");
			return;
		}
		application.restart(effective);
	}
}
