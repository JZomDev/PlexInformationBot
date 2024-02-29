package org.example.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.example.Main.PLEX_KEY;
import org.example.workers.ActivePlexUsersWorker;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class PlexListener implements SlashCommandCreateListener
{
	private static final Logger logger = LogManager.getLogger(PlexListener.class);

	public PlexListener()
	{
		logger.info("Plex Listener has started");
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
						String line = activePlexUsersWorker.execute(event.getApi(), PLEX_KEY).join();

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
