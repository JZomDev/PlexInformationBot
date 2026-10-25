package org.example.listeners;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DMListener implements MessageCreateListener
{
	private static final ScheduledExecutorService TIMEOUT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "dm-listener-timeout");
		thread.setDaemon(true);
		return thread;
	});

	DiscordApi api;
	long userID;
	PlexMediaServer plexMediaServer;
	private final ScheduledFuture<?> timeout;
	private final Runnable onDone;

	public String VALID_EMAIL_ADDRESS_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
		+ "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	public Pattern PATTERN_MATCH = Pattern.compile(VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);

	public DMListener(DiscordApi api, long userID, PlexMediaServer plexMediaServer, Runnable onDone)
	{
		this.api = api;
		this.userID = userID;
		this.plexMediaServer = plexMediaServer;
		this.onDone = onDone;
		timeout = TIMEOUT_SCHEDULER.schedule(this::onTimeout, 24, TimeUnit.HOURS);
	}

	public long getUserID()
	{
		return userID;
	}

	/** Cancels the pending timeout and detaches this listener from the api. */
	public void stop()
	{
		timeout.cancel(false);
		api.removeListener(this);
		onDone.run();
	}

	private void onTimeout()
	{
		api.removeListener(this);
		onDone.run();
		api.getUserById(userID).thenAccept(user ->
			user.sendMessage("Timed Out. Message Server Admin with your email so They Can Add You Manually."));
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event)
	{
		if (!event.isPrivateMessage())
		{
			return;
		}

		if (event.getMessageAuthor().getId() != userID)
		{
			return;
		}

		String msg = event.getMessageContent();
		if (validEmail(msg))
		{
			event.getChannel().sendMessage("Got it we will be adding your email to plex shortly!");

			boolean success = plexInvite(msg);

			if (success)
			{
				event.getChannel().sendMessage("You have Been Added To Plex! Login to plex and accept the invite!");
            }
			else
			{
				event.getChannel().sendMessage("There was an error adding this email address. Message Server Admin.");
            }
			// remove listener and cancel timer when plex invite attempted
			stop();
		}
		else
		{
			event.getChannel().sendMessage("Invalid email. Please just type in your email and nothing else.");
		}
	}

	private boolean plexInvite(String invitedEmail)
	{
		List<PlexServer.Section> sections = plexMediaServer.toPlexServer().getSections();
		// only keep show and movies
		sections.removeIf(section -> !section.getType().equals("show") && !section.getType().equals("movie"));

		try
		{
			plexMediaServer.toPlexServer().serverShares().inviteFriend(invitedEmail, sections);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	private boolean validEmail(String emailAddress)
	{
		return PATTERN_MATCH.matcher(emailAddress).matches();
	}
}
