package org.example.bot;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoginWaitTest
{
	@Test
	void returnsTheValueWhenItArrivesInTime()
	{
		List<String> late = new ArrayList<>();
		assertEquals("api", LoginWait.await(CompletableFuture.completedFuture("api"), 1, TimeUnit.SECONDS, late::add));
		assertTrue(late.isEmpty());
	}

	@Test
	void timeoutThrowsAndClosesALateArrival()
	{
		List<String> late = new ArrayList<>();
		CompletableFuture<String> future = new CompletableFuture<>();
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> LoginWait.await(future, 50, TimeUnit.MILLISECONDS, late::add));
		assertEquals("Discord login timed out", e.getMessage());
		assertTrue(late.isEmpty());
		future.complete("late-api");
		assertEquals(List.of("late-api"), late);
	}

	@Test
	void failureIsRethrownWithItsCause()
	{
		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(new IllegalArgumentException("boom"));
		CompletionException e = assertThrows(CompletionException.class,
				() -> LoginWait.await(future, 1, TimeUnit.SECONDS, s -> { }));
		assertInstanceOf(IllegalArgumentException.class, e.getCause());
	}
}
