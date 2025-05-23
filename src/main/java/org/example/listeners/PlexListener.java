package org.example.listeners;

import kekolab.javaplex.PlexMediaServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.workers.ActivePlexUsersWorker;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class PlexListener implements SlashCommandCreateListener
{
	private static final Logger logger = LogManager.getLogger(PlexListener.class);

	PlexMediaServer plexMediaServer;

	public PlexListener(PlexMediaServer plexMediaServer)
	{
		logger.info("Plex Listener has started");
		this.plexMediaServer = plexMediaServer;
	}

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event)
	{
		SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
		ActivePlexUsersWorker activePlexUsersWorker = new ActivePlexUsersWorker();

		if (slashCommandInteraction.getCommandName().equals("activestreams"))
		{
			slashCommandInteraction.respondLater(false)
				.thenAccept(interactionOriginalResponseUpdater -> {
					interactionOriginalResponseUpdater
						.setFlags(MessageFlag.LOADING)
						.update();

					try
					{
						String line = activePlexUsersWorker.execute(event.getApi()).join();

						slashCommandInteraction.createFollowupMessageBuilder()
							.setContent(line)
							.send()
							.join();

					}
					catch (Exception e)
					{
						interactionOriginalResponseUpdater
							.delete()
							.join();

						slashCommandInteraction.createFollowupMessageBuilder()
							.setContent("Failed to get count")
							.setFlags(MessageFlag.EPHEMERAL)
							.send()
							.join();
					}
				});
		}
	}
}
