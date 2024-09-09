package org.example.workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import kekolab.javaplex.PlexEpisode;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexMovie;
import kekolab.javaplex.PlexPart;
import kekolab.javaplex.PlexVideoStream;
import org.javacord.api.DiscordApi;

public class CountPlexUsersWorker
{
	public CompletableFuture<String> execute(DiscordApi api, PlexMediaServer plexMediaServer)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				try
				{

					List<PlexMediatag<?>> mediatags = plexMediaServer.status().sessions(); // A list of all the items being streamed

					int totalTotal = mediatags.size();
					int directPlayTotal = 0;
					int transcodeTotal = 0;
					int directPlayMovie = 0;
					int transcodeMovie = 0;
					int directPlayEpisode = 0;
					int transcodeEpisode = 0;
					for (int i = 0; i < mediatags.size(); i++)
					{
						PlexMediatag plexMediatag = mediatags.get(i);
						if (plexMediatag instanceof PlexMovie b)
						{
							b.getMedia().size();

							for (int j = 0; j < b.getMedia().size(); j++)
							{
								List<PlexPart> plexParts = b.getMedia().get(0).getParts();

								for (int jj = 0; jj < plexParts.size(); jj++)
								{
									PlexPart p = plexParts.get(jj);
									if (p.getDecision().equals("directplay"))
									{
										directPlayTotal++;
										directPlayMovie++;
									}
									else
									{
										transcodeTotal++;
										transcodeMovie++;
									}
								}
							}
						}
						if (plexMediatag instanceof PlexEpisode b)
						{
							b.getMedia().size();

							for (int j = 0; j < b.getMedia().size(); j++)
							{
								List<PlexPart> plexParts = b.getMedia().get(0).getParts();

								for (int jj = 0; jj < plexParts.size(); jj++)
								{
									PlexPart p = plexParts.get(jj);
									String decision = p.getDecision();
									if (decision == null)
									{
										// cannot get the decision for some reason, cringe
									}
									else
									{
										if (p.getDecision().equals("directplay"))
										{
											directPlayTotal++;
											directPlayEpisode++;
										}
										else
										{
											transcodeTotal++;
											transcodeEpisode++;
										}
									}
								}
							}
						}
					}

					String line = "unknown";
					if (totalTotal == 0)
					{
						line = "to 0 people";
					}
					else if (totalTotal == 1)
					{
						line = "to 1 person";
					}
					else
					{
						line = "to " + totalTotal + " people";
					}
					return line;
				}
				catch (Throwable t)
				{
					return "Unknown";
				}
			}
			catch (Exception e)
			{
				return "Unknown";
			}
		}, api.getThreadPool().getExecutorService());
	}
}
