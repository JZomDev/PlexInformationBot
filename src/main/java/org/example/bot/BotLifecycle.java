package org.example.bot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.Settings;

/**
 * Serialises bot restarts on a single thread. restart() returns immediately; if several restarts queue
 * up only the latest requested settings are applied. The old connection is always closed before a new
 * one is opened, so two connections are never open at once.
 */
public class BotLifecycle implements BotController
{
	public interface Connection
	{
		void close();
	}

	public interface Connector
	{
		Connection connect(Settings settings) throws Exception;
	}

	private static final Logger logger = LogManager.getLogger(BotLifecycle.class);

	private final Connector connector;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(r ->
	{
		Thread t = new Thread(r, "bot-lifecycle");
		t.setDaemon(true);
		return t;
	});
	private final AtomicReference<Settings> pending = new AtomicReference<>();
	private volatile BotState state = BotState.NOT_CONFIGURED;
	private Connection current; // only touched on the lifecycle thread

	public BotLifecycle(Connector connector)
	{
		this.connector = connector;
	}

	@Override
	public void restart(Settings effective)
	{
		pending.set(effective);
		state = BotState.STARTING;
		executor.execute(this::applyPending);
	}

	@Override
	public BotState state()
	{
		return state;
	}

	/** Blocks until every queued restart has been applied. */
	public void awaitIdle() throws Exception
	{
		executor.submit(() -> { }).get();
	}

	private void applyPending()
	{
		Settings settings = pending.getAndSet(null);
		if (settings == null)
		{
			return;
		}
		closeCurrent();
		if (settings.botToken().isEmpty())
		{
			finish(BotState.NOT_CONFIGURED);
			return;
		}
		try
		{
			current = connector.connect(settings);
			finish(BotState.RUNNING);
		}
		catch (Throwable e)
		{
			logger.error("Failed to start the Discord bot: {}", describe(e, settings.botToken()));
			finish(BotState.FAILED);
		}
	}

	private void finish(BotState result)
	{
		if (pending.get() == null)
		{
			state = result;
		}
	}

	private void closeCurrent()
	{
		Connection old = current;
		current = null;
		if (old != null)
		{
			try
			{
				old.close();
			}
			catch (Throwable e)
			{
				logger.warn("Error while stopping the bot: {}", e.getClass().getSimpleName());
			}
		}
	}

	private static String describe(Throwable e, String token)
	{
		Throwable root = e;
		while (root.getCause() != null && root.getCause() != root)
		{
			root = root.getCause();
		}
		String message = root.getMessage() == null ? "" : root.getMessage();
		if (!token.isEmpty())
		{
			message = message.replace(token, "********");
		}
		return root.getClass().getSimpleName() + (message.isEmpty() ? "" : ": " + message);
	}
}
