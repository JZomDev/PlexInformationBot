package org.example.listeners;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;
import org.example.config.AppConfig;
import org.example.config.Settings;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
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
	@Override
	public void onReactionAdd(ReactionAddEvent event)
	{
		if (isCorrectReaction(event) && isCorrectMessage(event) && !isBotUser(event))
		{
			User userReacted = event.getUser().get();
			Server server = event.getServer().get();
			Optional<Role> role = server.getRoleById(AppConfig.get().roleId());

			role.ifPresent(value -> userReacted.addRole(value).whenComplete((unused, error) -> {
					logger.info("Assigned role");
				}
			).exceptionally((e) ->
			{
				logger.error("Failed to Assigned role\n" + e.getMessage(), e);
				return null;
			}));
		}
	}

	@Override
	public void onReactionRemove(ReactionRemoveEvent event)
	{
		if (isCorrectReaction(event) && isCorrectMessage(event) && !isBotUser(event))
		{
			User userReacted = event.getUser().get();
			Server server = event.getServer().get();
			Optional<Role> role = server.getRoleById(AppConfig.get().roleId());

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

	public static boolean matchesRoleMessage(Settings s, long authorId, long botId, long messageId, String content)
	{
		if (!s.roleMessageId().isEmpty() && s.roleMessageId().equals(Long.toString(messageId)))
		{
			return true;
		}
		return authorId == botId && Main.DISCORD_MESSAGE.equals(content);
	}

	private boolean isCorrectMessage(SingleReactionEvent event)
	{
		long botUserID = event.getApi().getYourself().getId();
		long authorID = event.getMessageAuthor().map(a -> a.getId()).orElse(0L);
		String messageContent = event.getMessageContent().orElse("");
		return matchesRoleMessage(AppConfig.get(), authorID, botUserID, event.getMessageId(), messageContent);
	}

	private boolean isBotUser(SingleReactionEvent event)
	{
		return event.getApi().getYourself().getId() == event.getUser().get().getId();
	}
}
