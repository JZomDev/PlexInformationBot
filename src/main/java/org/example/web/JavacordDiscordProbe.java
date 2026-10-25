package org.example.web;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.concurrent.TimeUnit;

/** Real Discord check: logs in with a throwaway connection and disconnects again. */
public class JavacordDiscordProbe implements DiscordProbe
{
	@Override
	public CheckResult probe(String token)
	{
		DiscordApi api = null;
		try
		{
			api = new DiscordApiBuilder().setToken(token).setWaitForServersOnStartup(false)
					.setWaitForUsersOnStartup(false).login().orTimeout(15, TimeUnit.SECONDS).join();
			return new CheckResult(true, "Logged in as " + api.getYourself().getName() + " ("
					+ api.getServers().size() + " servers)");
		}
		catch (Exception e)
		{
			// never echo the exception: it may carry the token
			return new CheckResult(false, "Could not log in: check the token");
		}
		finally
		{
			if (api != null)
			{
				api.disconnect();
			}
		}
	}
}
