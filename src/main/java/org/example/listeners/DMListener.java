package org.example.listeners;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexServer;
import kekolab.javaplex.PlexServerShare;
import org.example.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DMListener implements MessageCreateListener
{
	DiscordApi api;
	long userID;
	PlexMediaServer plexMediaServer;
	Timer timer;

	public DMListener(DiscordApi api, long userID, PlexMediaServer plexMediaServer) throws Exception
	{
		this.api = api;
		this.userID = userID;
		this.plexMediaServer = plexMediaServer;
		timer = new Timer();
		timer.schedule(new StopListener(this), Date.from(Instant.now().plus(86400, ChronoUnit.SECONDS)));
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event)
	{
		if (!event.isPrivateMessage())
		{
			return;
		}

		if (event.getMessage().getUserAuthor().get().getId() == api.getYourself().getId())
		{
			return;
		}

		String msg = event.getMessageContent();
		if (validEmail(msg))
		{
			event.getChannel().sendMessage("Got it we will be adding your email to plex shortly!");
			try
			{
				boolean success = plexInvite(msg);

				if (success)
				{
					event.getChannel().sendMessage("You have Been Added To Plex! Login to plex and accept the invite!");
					event.getApi().removeListener(this);
				}
				else
				{
					event.getChannel().sendMessage("There was an error adding this email address. Message Server Admin.");
				}
			}
			catch (Exception e)
			{
				event.getChannel().sendMessage("There was an error, please contact the server administrator");
			}
		}
		else
		{
			event.getChannel().sendMessage("Invalid email. Please just type in your email and nothing else.");
		}
	}

	private boolean plexInvite(String invitedEmail) throws Exception
	{
		List<PlexServer.Section> sections = plexMediaServer.toPlexServer().getSections();
		// only keep show and movies
		sections.removeIf(section -> !section.getType().equals("show") && !section.getType().equals("movie"));
		PlexServerShare invitedUser = plexMediaServer.toPlexServer().serverShares().inviteFriend(invitedEmail, sections);

		return true;
	}

	private boolean validEmail(String emailAddress)
	{
		return Main.PATTERN_MATCH.matcher(emailAddress).matches();
	}

	class StopListener extends TimerTask
	{
		DMListener dmListener;

		public StopListener(DMListener dmListener)
		{
			this.dmListener = dmListener;
		}

		@Override
		public void run()
		{
			api.removeListener(dmListener);
			api.getUserById(userID).join().sendMessage("Timed Out. Message Server Admin with your email so They Can Add You Manually.");
		}
	}
}
