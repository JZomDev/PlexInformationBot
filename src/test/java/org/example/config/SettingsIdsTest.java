package org.example.config;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SettingsIdsTest
{
	private static Settings withStatusMessage(String id)
	{
		Settings e = Settings.empty();
		return new Settings("", "", "", "", "", "", "", "", "", "", "", "", "", "", id, "", "", "", "", "");
	}

	private static String digits(int n)
	{
		return "1".repeat(n);
	}

	@Test
	void emptyIsAccepted()
	{
		assertEquals(Optional.empty(), Settings.empty().invalidIdField());
	}

	@Test
	void seventeenAndTwentyDigitsAreAccepted()
	{
		assertEquals(Optional.empty(), withStatusMessage(digits(17)).invalidIdField());
		assertEquals(Optional.empty(), withStatusMessage(digits(20)).invalidIdField());
	}

	@Test
	void sixteenAndTwentyOneDigitsAreRejected()
	{
		assertTrue(withStatusMessage(digits(16)).invalidIdField().isPresent());
		assertTrue(withStatusMessage(digits(21)).invalidIdField().isPresent());
	}

	@Test
	void lettersInnerSpacesUnicodeDigitsAndSignsAreRejected()
	{
		assertTrue(withStatusMessage("12345678901234567a").invalidIdField().isPresent());
		assertTrue(withStatusMessage("12345678 901234567").invalidIdField().isPresent());
		assertTrue(withStatusMessage("١".repeat(18)).invalidIdField().isPresent());
		assertTrue(withStatusMessage("-12345678901234567").invalidIdField().isPresent());
		assertTrue(withStatusMessage("+12345678901234567").invalidIdField().isPresent());
	}

	@Test
	void surroundingWhitespaceIsTrimmedNotRejected()
	{
		Settings s = withStatusMessage("  " + digits(18) + "\t\n");
		assertEquals(Optional.empty(), s.invalidIdField());
		assertEquals(digits(18), s.trimmedIds().statusMessageId());
	}

	@Test
	void messageNamesTheFieldLabel()
	{
		assertEquals("Status message ID must be the number Discord shows, 17 to 20 digits",
				withStatusMessage("abc").invalidIdField().orElseThrow());
	}

	@Test
	void everyIdFieldIsChecked()
	{
		String bad = "x";
		String[] labels = {"Role ID", "Role channel ID", "Role message ID", "Status channel ID",
				"Status message ID", "Queue channel ID", "Queue message ID"};
		for (int i = 0; i < labels.length; i++)
		{
			String[] v = new String[17];
			java.util.Arrays.fill(v, "");
			v[10 + i] = bad;
			Settings s = new Settings(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11],
					v[12], v[13], v[14], v[15], v[16], "", "", "");
			assertTrue(s.invalidIdField().orElseThrow().startsWith(labels[i] + " must"), labels[i]);
		}
	}

	@Test
	void trimmedIdsLeavesOtherFieldsAlone()
	{
		Settings s = new Settings(" tok ", " 1.2.3.4 ", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
		assertEquals(" tok ", s.trimmedIds().botToken());
		assertEquals(" 1.2.3.4 ", s.trimmedIds().plexIp());
	}
}
