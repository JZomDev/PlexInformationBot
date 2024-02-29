package org.example.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.listener.server.ServerBecomesAvailableListener;

public class ServerBecomesAvailable implements ServerBecomesAvailableListener
{
	private static final Logger logger = LogManager.getLogger(ServerBecomesAvailable.class);

	@Override
	public void onServerBecomesAvailable(final ServerBecomesAvailableEvent event)
	{
		Server server = event.getServer();
		logger.info("Server became available: " + server.getName());
	}
}
