package org.example.config;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SettingsAnnounceTest
{
	private static final String CHANNEL = "11111111111111111";

	private static Settings announce(String channel, String movies, String seasons)
	{
		return new Settings("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
				channel, movies, seasons);
	}

	@Test
	void missingFieldsDeserialiseToEmpty()
	{
		Settings s = new Gson().fromJson("{\"botToken\":\"t\"}", Settings.class);
		assertEquals("", s.announceChannelId());
		assertEquals("", s.announceMoviesOn());
		assertEquals("", s.announceSeasonsOn());
	}

	@Test
	void moviesFalseWithChannelButBlankSwitch()
	{
		assertFalse(announce(CHANNEL, "", "").announceMovies());
	}

	@Test
	void moviesTrueWithChannelAndSwitch()
	{
		assertTrue(announce(CHANNEL, "true", "").announceMovies());
	}

	@Test
	void moviesFalseWithSwitchAndNoChannel()
	{
		assertFalse(announce("", "true", "true").announceMovies());
		assertFalse(announce("  ", "true", "true").announceSeasons());
	}

	@Test
	void seasonsFollowTheirOwnSwitch()
	{
		assertTrue(announce(CHANNEL, "", "true").announceSeasons());
		assertFalse(announce(CHANNEL, "true", "").announceSeasons());
	}

	@Test
	void nonNumericChannelIsReported()
	{
		Optional<String> err = announce("abc", "", "").invalidIdField();
		assertTrue(err.isPresent());
		assertTrue(err.get().startsWith("Announcement channel ID"));
		assertEquals(Optional.empty(), announce(CHANNEL, "", "").invalidIdField());
	}

	@Test
	void channelIsTrimmed()
	{
		assertEquals(CHANNEL, announce(" " + CHANNEL + " ", "", "").trimmedIds().announceChannelId());
	}

	@Test
	void maskedAndApplyFormLeaveSwitchesUntouched()
	{
		Settings s = announce(CHANNEL, "true", "true");
		assertEquals(s, s.masked());
		Settings saved = announce("", "", "");
		Settings applied = saved.applyForm(s);
		assertEquals("true", applied.announceMoviesOn());
		assertEquals("true", applied.announceSeasonsOn());
		assertEquals(CHANNEL, applied.announceChannelId());
	}
}
