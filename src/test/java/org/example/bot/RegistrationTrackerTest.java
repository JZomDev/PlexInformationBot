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
	void reportsSuccessOnlyAfterARegistrationWasMarkedDone()
	{
		RegistrationTracker t = new RegistrationTracker();
		assertFalse(t.anySucceeded());
		t.claim(1L);
		assertFalse(t.anySucceeded());
		t.succeeded(1L);
		assertTrue(t.anySucceeded());
		assertFalse(new RegistrationTracker().anySucceeded());
	}

	@Test
	void aNewTrackerStartsEmptyForANewConnection()
	{
		RegistrationTracker first = new RegistrationTracker();
		first.claim(1L);
		assertTrue(new RegistrationTracker().claim(1L));
	}
}
