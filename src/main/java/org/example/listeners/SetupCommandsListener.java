package org.example.listeners;

import java.io.IOException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;
import org.example.config.AppConfig;
import org.example.config.Settings;
import org.example.config.SettingsStore;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class SetupCommandsListener implements SlashCommandCreateListener
{
	private static final Logger logger = LogManager.getLogger(SetupCommandsListener.class);
	private static final String CLAPPERBOARD = "🎬";
	private static final String NOT_ALLOWED = "Only administrators can use this command.";
	private static final String SAVE_FAILED = "Settings could not be saved.";
	private static final String FAILED = "Setup failed. Check the bot's permissions in this channel.";

	private static final String BAD_ROLE = "That role cannot be used. Pick a normal role that is below the bot's own role.";

	private final SettingsStore store;

	public SetupCommandsListener(SettingsStore store)
	{
		this.store = store;
	}

	public static Settings apply(Settings fileValues, String command, String channelId, String messageId, String roleId)
	{
		Settings s = fileValues;
		switch (command)
		{
			case "setupreact":
				return new Settings(s.botToken(), s.plexIp(), s.plexPort(), s.plexToken(), s.tautulliUrl(),
						s.tautulliApiKey(), s.radarrUrl(), s.radarrApiKey(), s.sonarrUrl(), s.sonarrApiKey(),
						roleId, channelId, messageId, s.statusChannelId(), s.statusMessageId(),
						s.queueChannelId(), s.queueMessageId(),
						s.announceChannelId(), s.announceMoviesOn(), s.announceSeasonsOn());
			case "setupstatus":
				return new Settings(s.botToken(), s.plexIp(), s.plexPort(), s.plexToken(), s.tautulliUrl(),
						s.tautulliApiKey(), s.radarrUrl(), s.radarrApiKey(), s.sonarrUrl(), s.sonarrApiKey(),
						s.roleId(), s.roleChannelId(), s.roleMessageId(), channelId, messageId,
						s.queueChannelId(), s.queueMessageId(),
						s.announceChannelId(), s.announceMoviesOn(), s.announceSeasonsOn());
			case "setupcontentqueue":
				return new Settings(s.botToken(), s.plexIp(), s.plexPort(), s.plexToken(), s.tautulliUrl(),
						s.tautulliApiKey(), s.radarrUrl(), s.radarrApiKey(), s.sonarrUrl(), s.sonarrApiKey(),
						s.roleId(), s.roleChannelId(), s.roleMessageId(), s.statusChannelId(), s.statusMessageId(),
						channelId, messageId, s.announceChannelId(), s.announceMoviesOn(), s.announceSeasonsOn());
			case "setupannouncements":
				return new Settings(s.botToken(), s.plexIp(), s.plexPort(), s.plexToken(), s.tautulliUrl(),
						s.tautulliApiKey(), s.radarrUrl(), s.radarrApiKey(), s.sonarrUrl(), s.sonarrApiKey(),
						s.roleId(), s.roleChannelId(), s.roleMessageId(), s.statusChannelId(), s.statusMessageId(),
						s.queueChannelId(), s.queueMessageId(), channelId, "true", "true");
			default:
				return s;
		}
	}

	/** Null when the role can be handed out by the bot; otherwise the fixed text to reply with. */
	public static String roleProblem(boolean everyoneRole, boolean managed, boolean botCanManage)
	{
		return everyoneRole || managed || !botCanManage ? BAD_ROLE : null;
	}

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event)
	{
		SlashCommandInteraction interaction = event.getSlashCommandInteraction();
		String command = interaction.getCommandName();
		if (!command.equals("setupreact") && !command.equals("setupstatus") && !command.equals("setupcontentqueue")
				&& !command.equals("setupannouncements"))
		{
			return;
		}
		interaction.respondLater(true).thenAccept(updater -> {
			String reply;
			try
			{
				reply = run(interaction, command);
			}
			catch (Exception e)
			{
				logger.error("Setup command failed: {}", e.getClass().getSimpleName());
				reply = FAILED;
			}
			updater.setContent(reply).update();
		}).exceptionally(e -> {
			logger.error("Setup command could not respond: {}", e.getClass().getSimpleName());
			return null;
		});
	}

	private String run(SlashCommandInteraction interaction, String command) throws Exception
	{
		Optional<Server> server = interaction.getServer();
		Optional<TextChannel> channel = interaction.getChannel();
		if (server.isEmpty() || channel.isEmpty() || !server.get().hasPermission(interaction.getUser(), PermissionType.ADMINISTRATOR))
		{
			return NOT_ALLOWED;
		}
		String roleId = "";
		if (command.equals("setupreact"))
		{
			Optional<Role> role = interaction.getArgumentRoleValueByName("role");
			if (role.isEmpty())
			{
				return "Pick a role.";
			}
			String problem = roleProblem(role.get().isEveryoneRole(), role.get().isManaged(),
					server.get().canManageRole(interaction.getApi().getYourself(), role.get()));
			if (problem != null)
			{
				return problem;
			}
			roleId = role.get().getIdAsString();
		}
		if (command.equals("setupannouncements"))
		{
			String channelId = channel.get().getIdAsString();
			try
			{
				store.update(saved -> apply(saved, command, channelId, "", ""));
				AppConfig.reload();
			}
			catch (IOException e)
			{
				return SAVE_FAILED;
			}
			catch (Throwable e)
			{
				logger.error("Setup command failed: {}", e.getClass().getSimpleName());
				return FAILED;
			}
			return "Done. Announcements will post in this channel.";
		}
		Message message = command.equals("setupreact")
				? channel.get().sendMessage(Main.DISCORD_MESSAGE).join()
				: channel.get().sendMessage(new EmbedBuilder().setTitle("Loading...")).join();
		try
		{
			if (command.equals("setupreact"))
			{
				message.addReaction(CLAPPERBOARD).join();
			}
			String channelId = channel.get().getIdAsString();
			String messageId = message.getIdAsString();
			String role = roleId;
			store.update(saved -> apply(saved, command, channelId, messageId, role));
			AppConfig.reload();
		}
		catch (IOException e)
		{
			deleteQuietly(message);
			return SAVE_FAILED;
		}
		catch (Throwable e)
		{
			logger.error("Setup command failed after posting: {}", e.getClass().getSimpleName());
			deleteQuietly(message);
			return FAILED;
		}
		return "Done. This message is now set up in this channel.";
	}

	/** Best effort: a left-over message would still be treated as the role message by the legacy text rule. */
	private static void deleteQuietly(Message message)
	{
		try
		{
			message.delete().exceptionally(x -> null);
		}
		catch (Throwable ignored)
		{
		}
	}
}
