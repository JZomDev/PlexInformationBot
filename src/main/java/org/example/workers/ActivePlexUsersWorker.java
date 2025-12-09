package org.example.workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import kekolab.javaplex.PlexEpisode;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexMovie;
import kekolab.javaplex.PlexPart;
import org.example.Application;
import org.javacord.api.DiscordApi;

public class ActivePlexUsersWorker
{
	public CompletableFuture<String> execute(DiscordApi api, Application application)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				List<PlexMediatag<?>> mediatags = application.getSessions(); // A list of all the items being streamed

				int totalTotal = mediatags.size();
				int directPlayTotal = 0;
				int transcodeTotal = 0;
				for (PlexMediatag<?> plexMediatag : mediatags)
				{
					if (plexMediatag instanceof PlexMovie plexMovie)
					{
						for (int j = 0; j < plexMovie.getMedia().size(); j++)
						{
							List<PlexPart> plexParts = plexMovie.getMedia().get(0).getParts();

							for (PlexPart p : plexParts)
							{
								if (p.getDecision().equals("directplay"))
								{
									directPlayTotal++;
								}
								else
								{
									transcodeTotal++;
								}
							}
						}
					}
					if (plexMediatag instanceof PlexEpisode plexEpisode)
					{
						for (int j = 0; j < plexEpisode.getMedia().size(); j++)
						{
							List<PlexPart> plexParts = plexEpisode.getMedia().get(0).getParts();

							for (PlexPart p : plexParts)
							{
								if (p.getDecision().equals("directplay"))
								{
									directPlayTotal++;
								}
								else
								{
									transcodeTotal++;
								}
							}
						}
					}
				}

				String line = "Nobody is watching anything";
				if (totalTotal > 0)
				{
					if (totalTotal == directPlayTotal)
					{
						if (totalTotal == 1)
						{
							line = "One person is streaming via direct play";
						}
						else
						{
							line = totalTotal + " users streaming via direct play";
						}
					}
					else if (totalTotal == transcodeTotal)
					{
						if (totalTotal == 1)
						{
							line = "One person is streaming via transcoding";
						}
						else
						{
							line = totalTotal + " users streaming via transcoding";
						}
					}
					else
					{
						line = totalTotal + " users are streaming.\n";
						if (transcodeTotal == 1)
						{
							line = line + "One person is streaming via transcoding\n";
						}
						else
						{
							line = line + transcodeTotal + " users streaming via transcoding\n";
						}
						if (directPlayTotal == 1)
						{
							line = line + "One person is streaming via direct play";
						}
						else
						{
							line = line + directPlayTotal + " users streaming via direct play";
						}
					}
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
