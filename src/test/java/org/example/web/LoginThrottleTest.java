package org.example.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LoginThrottleTest
{
	@Test
	void blocksAfterFiveFailuresThenRecovers()
	{
		MutableClock clock = new MutableClock();
		LoginThrottle t = new LoginThrottle(clock);
		for (int i = 0; i < 4; i++)
		{
			t.failure("a");
		}
		assertTrue(t.allowed("a"));
		t.failure("a");
		assertFalse(t.allowed("a"));
		clock.advance(Duration.ofSeconds(29));
		assertFalse(t.allowed("a"));
		clock.advance(Duration.ofSeconds(2));
		assertTrue(t.allowed("a"));
	}

	@Test
	void successResetsCount()
	{
		LoginThrottle t = new LoginThrottle(new MutableClock());
		for (int i = 0; i < 4; i++)
		{
			t.failure("a");
		}
		t.success("a");
		for (int i = 0; i < 4; i++)
		{
			t.failure("a");
		}
		assertTrue(t.allowed("a"));
	}

	@Test
	void keysAreIndependent()
	{
		LoginThrottle t = new LoginThrottle(new MutableClock());
		for (int i = 0; i < 5; i++)
		{
			t.failure("a");
		}
		assertFalse(t.allowed("a"));
		assertTrue(t.allowed("b"));
	}
}
