package org.example.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import kekolab.javaplex.PlexMediaServer;
import org.example.config.AppConfig;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

public class RoleListener implements UserRoleAddListener, UserRoleRemoveListener
{
	private final Map<Long, DMListener> activeListeners = new ConcurrentHashMap<>();

	private final Supplier<PlexMediaServer> plexMediaServer;

	public RoleListener(Supplier<PlexMediaServer> plexMediaServer)
	{
		this.plexMediaServer = plexMediaServer;
	}

	private static boolean isConfiguredRole(long roleId)
	{
		return String.valueOf(roleId).equals(AppConfig.get().roleId().trim());
	}

	@Override
	public void onUserRoleAdd(UserRoleAddEvent event)
	{
		if (isConfiguredRole(event.getRole().getId()))
		{
			PlexMediaServer server = plexMediaServer.get();
			if (server == null)
			{
				return;
			}
			long userID = event.getUser().getId();
			event.getUser().sendMessage("Welcome To " + server.getFriendlyName() + ". Just reply with your email so we can add you to Plex!");
			event.getUser().sendMessage("I will wait 24 hours for your message, if you do not send it by then I will cancel the command.");

			DMListener[] holder = new DMListener[1];
			DMListener dmListener = new DMListener(event.getApi(), userID, server, () -> activeListeners.remove(userID, holder[0]));
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
		if (isConfiguredRole(event.getRole().getId()))
		{
			DMListener dmListener = activeListeners.remove(event.getUser().getId());
			if (dmListener != null)
			{
				dmListener.stop();
			}
		}
	}
}
