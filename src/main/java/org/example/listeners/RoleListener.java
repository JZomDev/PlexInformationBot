package org.example.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kekolab.javaplex.PlexMediaServer;
import org.example.Main;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

public class RoleListener implements UserRoleAddListener, UserRoleRemoveListener
{
	private final Map<Long, DMListener> activeListeners = new ConcurrentHashMap<>();

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

			DMListener[] holder = new DMListener[1];
			DMListener dmListener = new DMListener(event.getApi(), userID, plexMediaServer, () -> activeListeners.remove(userID, holder[0]));
			holder[0] = dmListener;
			DMListener previous = activeListeners.put(userID, dmListener);
			if (previous != null)
			{
				previous.stop();
			}
			event.getApi().addListener(dmListener);
		}
	}

	@Override
	public void onUserRoleRemove(UserRoleRemoveEvent event)
	{
		if (event.getRole().getId() == Long.parseLong(Main.ROLE_ID))
		{
			DMListener dmListener = activeListeners.remove(event.getUser().getId());
			if (dmListener != null)
			{
				dmListener.stop();
			}
		}
	}
}
