package org.plexinfobot.workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import kekolab.javaplex.PlexMediatag;
import org.javacord.api.DiscordApi;

public class CountPlexUsersWorker
{
	public CompletableFuture<String> execute(DiscordApi api, List<PlexMediatag<?>> mediatags)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				int totalTotal = mediatags.size();

				String line = "Streaming ";
				if (totalTotal == 0)
				{
					line += "to 0 people";
				}
				else if (totalTotal == 1)
				{
					line += "to 1 person";
				}
				else
				{
					line += "to " + totalTotal + " people";
				}
				return line;
			}
			catch (Exception e)
			{
				return "Unknown";
			}
		}, api.getThreadPool().getExecutorService());
	}
}
