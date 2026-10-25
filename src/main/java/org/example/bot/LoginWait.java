package org.example.bot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/** Waits for a Discord login with a limit so one hung login cannot block every later restart. */
public final class LoginWait
{
	private LoginWait()
	{
	}

	/**
	 * Returns the login result, or throws. When the wait gives up, a result that arrives later is handed to
	 * {@code onLate} (to disconnect it); the future is deliberately not cancelled, as a cancelled future
	 * would ignore the late result and leak the connection. Exception messages never carry the token.
	 */
	public static <T> T await(CompletableFuture<T> future, long timeout, TimeUnit unit, Consumer<T> onLate)
	{
		try
		{
			return future.get(timeout, unit);
		}
		catch (TimeoutException e)
		{
			future.thenAccept(onLate);
			throw new IllegalStateException("Discord login timed out");
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			future.thenAccept(onLate);
			throw new IllegalStateException("Discord login interrupted");
		}
		catch (ExecutionException e)
		{
			throw new CompletionException(e.getCause());
		}
	}
}
