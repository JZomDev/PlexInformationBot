package org.example.web;

import org.javacord.api.DiscordApi;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JavacordDiscordProbeTest
{
	private static DiscordApi fakeApi(AtomicInteger disconnects)
	{
		return (DiscordApi) Proxy.newProxyInstance(DiscordApi.class.getClassLoader(), new Class<?>[]{DiscordApi.class},
				(proxy, method, args) ->
				{
					if (method.getName().equals("disconnect"))
					{
						disconnects.incrementAndGet();
					}
					return null;
				});
	}

	@Test
	void lateLoginAfterTimeoutIsDisconnected()
	{
		AtomicInteger disconnects = new AtomicInteger();
		CompletableFuture<DiscordApi> login = new CompletableFuture<>();
		JavacordDiscordProbe probe = new JavacordDiscordProbe(t -> login, 50);
		CheckResult r = probe.probe("SECRET123");
		assertFalse(r.ok());
		assertFalse(r.message().contains("SECRET123"));
		assertEquals(0, disconnects.get());
		login.complete(fakeApi(disconnects));
		assertEquals(1, disconnects.get());
	}

	@Test
	void failedLoginIsNotOkAndHidesToken()
	{
		CompletableFuture<DiscordApi> login = new CompletableFuture<>();
		login.completeExceptionally(new RuntimeException("bad SECRET123"));
		CheckResult r = new JavacordDiscordProbe(t -> login, 50).probe("SECRET123");
		assertFalse(r.ok());
		assertFalse(r.message().contains("SECRET123"));
	}
}
