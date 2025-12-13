package org.plexinfobot.listeners;

import org.plexinfobot.Main;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class MessageListener implements MessageCreateListener
{

	@Override
	public void onMessageCreate(MessageCreateEvent event)
	{
		if (event.isPrivateMessage())
		{
			return;
		}

		Message msg = event.getMessage();

		if (!msg.getAuthor().isServerAdmin())
		{
			return;
		}

		if (msg.getContent().equals("!addreactmessage"))
		{
			event.getServerTextChannel().get().sendMessage(Main.DISCORD_MESSAGE)
				.thenAccept(message -> message.addReaction("\uD83C\uDFAC"));
		}
	}
}
