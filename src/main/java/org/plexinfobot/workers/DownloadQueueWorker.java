package org.plexinfobot.workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.plexinfobot.Application;

public class DownloadQueueWorker
{
	public DownloadQueueWorker()
	{

	}

	public CompletableFuture<EmbedBuilder> execute(DiscordApi api, Application application)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Download Queue");
				List<Application.DownloadDetails> getQueueDetailsRadarr = application.getQueueDetailsRadarr();

				embed.addField("Movie Queue", String.valueOf(getQueueDetailsRadarr.size()));
				if (getQueueDetailsRadarr.size() > 0)
				{
					for (Application.DownloadDetails downloadDetails : getQueueDetailsRadarr)
					{
						String name = downloadDetails.getOutputPath();

						embed.addField(name, downloadDetails.getEstimatedCompletionTime());

					}
				}

				return embed;
			}
			catch (Exception e)
			{
				return null;
			}
		}, api.getThreadPool().getExecutorService());
	}
}
