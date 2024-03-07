package org.example.listeners;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

public class ReactListener implements ReactionAddListener, ReactionRemoveListener
{
	private static final Logger logger = LogManager.getLogger(ReactListener.class);
	String roleID;

	public ReactListener(String roleID)
	{
		this.roleID = roleID;
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event)
	{
		String msgToReact = Main.DISCORD_MESSAGE;
		long botUserID = event.getApi().getYourself().getId();
		Message MessageReacted = event.requestMessage().join();
		Server server = event.getServer().get();
		User userReacted = event.getUser().get();
		if (userReacted.getId() == botUserID)
		{
			return;
		}

		Emoji emoji = event.getEmoji();

		if (!emoji.equalsEmoji("\uD83C\uDFAC"))
		{
			return;
		}

		if (MessageReacted.getAuthor().getId() == botUserID && MessageReacted.getContent().equals(msgToReact))
		{
			Optional<Role> role = event.getServer().get().getRoleById(roleID);

			if (role.isPresent())
			{
				Role presentRole = role.get();
				if (!event.getUser().get().getRoles(server).contains(presentRole))
				{
					userReacted.addRole(presentRole)
						.whenComplete((unused, throwable) -> {
							logger.info("Assigned role");
						})
						.exceptionally((e) ->
						{
							logger.error("Failed to assign role\n" + e.getMessage(), e);
							return null;
						});
				}
			}
		}
	}

	@Override
	public void onReactionRemove(ReactionRemoveEvent event)
	{
		String msgToReact = Main.DISCORD_MESSAGE;
		long botUserID = event.getApi().getYourself().getId();
		Message MessageReacted = event.requestMessage().join();

		Server server = event.getServer().get();
		User userReacted = event.getUser().get();
		if (userReacted.getId() == botUserID)
		{
			return;
		}

		Emoji emoji = event.getEmoji();

		if (!emoji.equalsEmoji("\uD83C\uDFAC"))
		{
			return;
		}

		if (MessageReacted.getAuthor().getId() == botUserID && MessageReacted.getContent().equals(msgToReact))
		{
			Optional<Role> role = server.getRoleById(roleID);

			role.ifPresent(value -> userReacted.removeRole(value).whenComplete((unused, error) -> {
					logger.info("Removed role");
				}
			).exceptionally((e) ->
			{
				logger.error("Failed to remove role\n" + e.getMessage(), e);
				return null;
			}));
		}
	}
}
