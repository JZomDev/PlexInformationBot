package org.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class StatusRefreshTest
{
	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

	@Test
	void changedSignatureEditsRightAfterAnEdit()
	{
		assertTrue(Application.shouldEdit("b", "a", 101 * SECOND, 100 * SECOND));
	}

	@Test
	void sameSignatureUnderIntervalDoesNotEdit()
	{
		assertFalse(Application.shouldEdit("a", "a", 114 * SECOND, 100 * SECOND));
	}

	@Test
	void sameSignatureAtIntervalEdits()
	{
		assertTrue(Application.shouldEdit("a", "a", 115 * SECOND, 100 * SECOND));
	}

	@Test
	void neverEditedEdits()
	{
		assertTrue(Application.shouldEdit("a", "a", 5 * SECOND, Application.NEVER_EDITED));
	}
}
