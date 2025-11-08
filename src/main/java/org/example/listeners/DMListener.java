package org.example.listeners;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexServer;
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

	public DMListener(DiscordApi api, long userID, PlexMediaServer plexMediaServer)
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

		if(event.getMessageAuthor().getId() == api.getYourself().getId())
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
            event.getApi().removeListener(this);
            timer.cancel();
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
			if (dmListener != null)
			{
				api.getUserById(userID).join().sendMessage("Timed Out. Message Server Admin with your email so They Can Add You Manually.");
			}
		}
	}
}
