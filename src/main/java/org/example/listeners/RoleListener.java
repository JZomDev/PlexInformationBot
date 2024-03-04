package org.example.listeners;

import org.example.Main;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

public class RoleListener implements UserRoleAddListener, UserRoleRemoveListener
{

	String machineID;
	String plexServerName;

	public RoleListener(String plexServerName, String machineID)
	{
		this.plexServerName = plexServerName;
		this.machineID = machineID;
	}

	@Override
	public void onUserRoleAdd(UserRoleAddEvent event)
	{
		if (event.getRole().getId() == Long.parseLong(Main.ROLE_ID))
		{
			long userID = event.getUser().getId();
			event.getUser().sendMessage("Welcome To " + plexServerName + ". Just reply with your email so we can add you to Plex!");
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
							System.out.println("Listener removed & cancelled, adding a new instance");
						}
					}
				}
				MessageCreateListener messageCreateListener = new DMListener(userID, machineID, event.getApi());
				event.getApi().addListener(messageCreateListener);
				System.out.println("Listener added");
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
