package org.example.web;

import org.example.listeners.SetupCommandsListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.exception.NotFoundException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/** Checks messages through the live bot connection. Answers are fixed texts: no exception text, no token. */
public class JavacordMessageProbe implements MessageProbe
{
	static final String NOT_CONNECTED = "The bot is not connected yet. Save the Discord token first.";
	static final String NO_CHANNEL = "The bot cannot see that channel. Check the channel ID and the bot's permissions.";
	static final String NO_MESSAGE = "That message was not found in that channel";
	static final String NOT_ALLOWED = "The bot is not allowed to read that channel";
	static final String NOT_BY_BOT = "That message was written by someone else, so the bot cannot edit it";
	static final String FAILED = "Could not check the message";
	static final String TIMED_OUT = "Timed out after 10 seconds";
	static final String NO_ROLE = "That role was not found in the server of that channel";
	static final String NOT_SERVER_TEXT = "That is not a text channel in a server";
	static final String FAILED_CHANNEL = "Could not check the channel";
	private static final long TIMEOUT_SECONDS = 10;

	private final Supplier<DiscordApi> live;

	public JavacordMessageProbe(Supplier<DiscordApi> live)
	{
		this.live = live;
	}

	/** Pure decision once the message is found. Names are remote text; the page renders them as text. */
	static CheckResult verdict(boolean writtenByBot, String botName, String channelName)
	{
		if (!writtenByBot)
		{
			return new CheckResult(false, NOT_BY_BOT);
		}
		return new CheckResult(true, "Found a message from " + botName + " in #" + channelName
				+ "; the bot can edit it");
	}

	@Override
	public CheckResult probe(String channelId, String messageId)
	{
		DiscordApi api = live.get();
		if (api == null)
		{
			return new CheckResult(false, NOT_CONNECTED);
		}
		try
		{
			Optional<TextChannel> channel = api.getTextChannelById(channelId);
			if (channel.isEmpty())
			{
				return new CheckResult(false, NO_CHANNEL);
			}
			Message message = api.getMessageById(messageId, channel.get()).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			String name = channel.get().asServerTextChannel().map(ServerTextChannel::getName).orElse("channel");
			return verdict(message.getAuthor().isYourself(), api.getYourself().getName(), name);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return new CheckResult(false, "Interrupted");
		}
		catch (TimeoutException e)
		{
			return new CheckResult(false, TIMED_OUT);
		}
		catch (ExecutionException e)
		{
			Throwable cause = e.getCause();
			if (cause instanceof NotFoundException)
			{
				return new CheckResult(false, NO_MESSAGE);
			}
			return new CheckResult(false, cause instanceof MissingPermissionsException ? NOT_ALLOWED : FAILED);
		}
		catch (RuntimeException e)
		{
			return new CheckResult(false, FAILED);
		}
	}

	/** Pure decision from what the bot may do in the channel. The name is remote text; the page renders it as text. */
	static CheckResult channelVerdict(String channelName, boolean canSee, boolean canSend, boolean canEmbed)
	{
		if (!canSee)
		{
			return new CheckResult(false, NO_CHANNEL);
		}
		if (!canSend)
		{
			return new CheckResult(false, "The bot is not allowed to send messages in #" + channelName);
		}
		if (!canEmbed)
		{
			return new CheckResult(false, "The bot is not allowed to embed links in #" + channelName);
		}
		return new CheckResult(true, "Found #" + channelName + ". The bot can post here.");
	}

	@Override
	public CheckResult probeChannel(String channelId)
	{
		DiscordApi api = live.get();
		if (api == null)
		{
			return new CheckResult(false, NOT_CONNECTED);
		}
		try
		{
			Optional<ServerTextChannel> channel = api.getServerTextChannelById(channelId);
			if (channel.isEmpty())
			{
				return new CheckResult(false, api.getTextChannelById(channelId).isPresent() ? NOT_SERVER_TEXT : NO_CHANNEL);
			}
			ServerTextChannel c = channel.get();
			User me = api.getYourself();
			return channelVerdict(c.getName(), c.canSee(me), c.canWrite(me), c.hasPermission(me, PermissionType.EMBED_LINKS));
		}
		catch (RuntimeException e)
		{
			return new CheckResult(false, FAILED_CHANNEL);
		}
	}

	@Override
	public CheckResult probeRole(String roleId, String channelId)
	{
		DiscordApi api = live.get();
		if (api == null)
		{
			return new CheckResult(false, NOT_CONNECTED);
		}
		try
		{
			Optional<Server> server = api.getTextChannelById(channelId).flatMap(TextChannel::asServerChannel)
					.map(c -> c.getServer());
			if (server.isEmpty())
			{
				return new CheckResult(false, NO_CHANNEL);
			}
			Optional<Role> role = server.get().getRoleById(roleId);
			if (role.isEmpty())
			{
				return new CheckResult(false, NO_ROLE);
			}
			String problem = SetupCommandsListener.roleProblem(role.get().isEveryoneRole(), role.get().isManaged(),
					server.get().canManageRole(api.getYourself(), role.get()));
			return problem == null ? new CheckResult(true, "The role can be handed out by the bot")
					: new CheckResult(false, problem);
		}
		catch (RuntimeException e)
		{
			return new CheckResult(false, FAILED);
		}
	}
}
