package org.example.web;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** Real Discord check: logs in with a throwaway connection and disconnects again. */
public class JavacordDiscordProbe implements DiscordProbe
{
	private static final long TIMEOUT_MILLIS = 10_000;

	private final Function<String, CompletableFuture<DiscordApi>> login;
	private final long timeoutMillis;

	public JavacordDiscordProbe()
	{
		this(token -> new DiscordApiBuilder().setToken(token).setWaitForServersOnStartup(false)
				.setWaitForUsersOnStartup(false).login());
	}

	JavacordDiscordProbe(Function<String, CompletableFuture<DiscordApi>> login)
	{
		this(login, TIMEOUT_MILLIS);
	}

	JavacordDiscordProbe(Function<String, CompletableFuture<DiscordApi>> login, long timeoutMillis)
	{
		this.login = login;
		this.timeoutMillis = timeoutMillis;
	}

	@Override
	public CheckResult probe(String token)
	{
		CompletableFuture<DiscordApi> future = null;
		try
		{
			future = login.apply(token);
			DiscordApi api = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
			return new CheckResult(true, "Logged in as " + api.getYourself().getName() + " ("
					+ api.getServers().size() + " servers)");
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return new CheckResult(false, "Interrupted");
		}
		catch (Exception e)
		{
			// never echo the exception: it may carry the token
			return new CheckResult(false, "Could not log in: check the token");
		}
		finally
		{
			if (future != null)
			{
				// Closes the connection when the login completes: at once on success, or later if it
				// arrives after a timeout. The future is deliberately not cancelled: a cancelled future
				// ignores the late result and the connection would leak.
				future.thenAccept(DiscordApi::disconnect);
			}
		}
	}
}
