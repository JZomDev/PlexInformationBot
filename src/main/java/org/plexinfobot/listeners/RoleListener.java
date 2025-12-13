package org.plexinfobot.listeners;

import kekolab.javaplex.PlexMediaServer;
import org.plexinfobot.Main;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

public class RoleListener implements UserRoleAddListener, UserRoleRemoveListener
{

	PlexMediaServer plexMediaServer;
	public RoleListener(PlexMediaServer plexMediaServer)
	{
		this.plexMediaServer = plexMediaServer;
	}

	@Override
	public void onUserRoleAdd(UserRoleAddEvent event)
	{
		if (event.getRole().getId() == Long.parseLong(Main.ROLE_ID))
		{
			long userID = event.getUser().getId();
			event.getUser().sendMessage("Welcome To " + plexMediaServer.getFriendlyName() + ". Just reply with your email so we can add you to Plex!");
			event.getUser().sendMessage("I will wait 24 hours for your message, if you do not send it by then I will cancel the command.");
			try
			{
				for (GloballyAttachableListener listenerAttached : event.getApi().getListeners().keySet())
				{
					if (listenerAttached instanceof DMListener dmListener)
					{
						if (userID == dmListener.userID)
						{
							((DMListener) listenerAttached).timer.cancel();
							event.getApi().removeListener(listenerAttached);
						}
					}
				}
				MessageCreateListener messageCreateListener = new DMListener(event.getApi(), userID, plexMediaServer);
				event.getApi().addListener(messageCreateListener);
			}
			catch (Exception e)
			{
				// ignored
			}
		}
	}

	@Override
	public void onUserRoleRemove(UserRoleRemoveEvent event)
	{
		if (event.getRole().getId() == Long.parseLong(Main.ROLE_ID))
		{
			long userID = event.getUser().getId();
			for (GloballyAttachableListener listenerAttached : event.getApi().getListeners().keySet())
			{
				if (listenerAttached instanceof DMListener dmListener)
				{
					if (userID == dmListener.userID)
					{
						((DMListener) listenerAttached).timer.cancel();
						event.getApi().removeListener(listenerAttached);
					}
				}
			}
		}
	}
}
