package org.plexinfobot.listeners;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.plexinfobot.Main;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
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
		if (isCorrectReaction(event) && isCorrectMessage(event) && !isBotUser(event))
		{
			if (event.getUser().isEmpty() || event.getServer().isEmpty())
			{
				return;
			}

			User userReacted = event.getUser().get();
			Server server = event.getServer().get();
			Optional<Role> role = server.getRoleById(roleID);
			if (role.isEmpty())
			{
				logger.warn("Role {} not found when processing reaction add", roleID);
				return;
			}

			Role assignedRole = role.get();
			userReacted.addRole(assignedRole).whenComplete((unused, error) -> {
				if (error == null)
				{
					logger.debug("Assigned role {} to user {}", assignedRole.getIdAsString(), userReacted.getIdAsString());
				}
				else
				{
					logger.warn("Failed to assign role {} to user {}: {}", assignedRole.getIdAsString(), userReacted.getIdAsString(), error.getMessage());
				}
			});
		}
	}

	@Override
	public void onReactionRemove(ReactionRemoveEvent event)
	{
		if (isCorrectReaction(event) && isCorrectMessage(event) && !isBotUser(event))
		{
			if (event.getUser().isEmpty() || event.getServer().isEmpty())
			{
				return;
			}

			User userReacted = event.getUser().get();
			Server server = event.getServer().get();
			Optional<Role> role = server.getRoleById(roleID);
			if (role.isEmpty())
			{
				logger.warn("Role {} not found when processing reaction remove", roleID);
				return;
			}

			Role removedRole = role.get();
			userReacted.removeRole(removedRole).whenComplete((unused, error) -> {
				if (error == null)
				{
					logger.debug("Removed role {} from user {}", removedRole.getIdAsString(), userReacted.getIdAsString());
				}
				else
				{
					logger.warn("Failed to remove role {} from user {}: {}", removedRole.getIdAsString(), userReacted.getIdAsString(), error.getMessage());
				}
			});
		}
	}

	private boolean isCorrectReaction(SingleReactionEvent event)
	{
		if (event instanceof ReactionAddEvent || event instanceof ReactionRemoveEvent)
		{
			Emoji emoji = event.getEmoji();

			if (!emoji.equalsEmoji("\uD83C\uDFAC"))
			{
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isCorrectMessage(SingleReactionEvent event)
	{
		long botUserID = event.getApi().getYourself().getId();
		if (event.getMessageAuthor().isPresent())
		{
			long authorID = event.getMessageAuthor().get().getId();
			if (botUserID != authorID)
			{
				return false;
			}
		}
		if (event.getMessageContent().isEmpty())
		{
			return false;
		}
		if (event.getMessageContent().isPresent())
		{
			String messageContent = event.getMessageContent().get();
			return messageContent.equals(Main.DISCORD_MESSAGE);
		}
		return false;
	}

	private boolean isBotUser(SingleReactionEvent event)
	{
		if (event.getUser().isEmpty())
		{
			return false;
		}
		return event.getApi().getYourself().getId() == event.getUser().get().getId();
	}
}
