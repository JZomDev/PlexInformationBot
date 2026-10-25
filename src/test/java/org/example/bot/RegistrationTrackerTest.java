package org.example.bot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTrackerTest
{
	@Test
	void firstClaimWinsAndRepeatsAreSkipped()
	{
		RegistrationTracker t = new RegistrationTracker();
		assertTrue(t.claim(1L));
		assertFalse(t.claim(1L));
		assertTrue(t.claim(2L));
	}

	@Test
	void releasedServerCanBeClaimedAgain()
	{
		RegistrationTracker t = new RegistrationTracker();
		assertTrue(t.claim(1L));
		t.release(1L);
		assertTrue(t.claim(1L));
	}

	@Test
	void aNewTrackerStartsEmptyForANewConnection()
	{
		RegistrationTracker first = new RegistrationTracker();
		first.claim(1L);
		assertTrue(new RegistrationTracker().claim(1L));
	}
}
