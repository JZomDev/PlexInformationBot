package org.example.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** The page has no JS test runner; these check the markup and script carry the invite, health and checklist pieces. */
class SetupPageTest
{
	private static String resource(String name) throws IOException
	{
		try (var in = SetupPageTest.class.getResourceAsStream("/web/" + name))
		{
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	void inviteLinkOpensInANewTabAndIsHiddenWithoutAUrl() throws Exception
	{
		String js = resource("app.js");
		assertTrue(js.contains("Invite the bot to your server"));
		assertTrue(js.contains("target: '_blank', rel: 'noopener noreferrer'"));
		assertTrue(js.contains("api('GET', '/api/invite')"));
		assertTrue(js.contains("a.hidden = true"));
		assertFalse(js.contains("innerHTML"));
	}

	@Test
	void everySectionHasALabelledHealthDotPolledEvery30Seconds() throws Exception
	{
		String js = resource("app.js");
		String css = resource("style.css");
		assertTrue(js.contains("id: 'health-' + s.id"));
		assertTrue(js.contains("'aria-hidden': 'true'"));
		for (String label : new String[] {"Connected", "Unreachable", "Not set"})
		{
			assertTrue(js.contains("'" + label + "'"), label);
		}
		assertTrue(js.contains("HEALTH_EVERY_MS = 30000"));
		assertTrue(js.contains("document.hidden"));
		assertTrue(js.contains("stopHealth();"));
		assertTrue(css.contains(".health.state-ok .dot"));
		assertTrue(css.contains(".health.state-down .dot"));
	}

	@Test
	void checklistCardSitsBelowTheStatusBoxAndCanBeBroughtBack() throws Exception
	{
		String html = resource("index.html");
		String js = resource("app.js");
		String css = resource("style.css");
		int top = html.indexOf("aria-label=\"Bot status and tools\"");
		int card = html.indexOf("id=\"setup-checklist\"");
		assertTrue(top > 0 && card > top);
		assertTrue(card < html.indexOf("id=\"warning\""));
		assertTrue(html.contains("id=\"checklist-show\""));
		assertTrue(html.contains("Show setup checklist"));
		assertTrue(js.contains("setupCommandsRegistered"));
		assertTrue(js.contains("/setupstatus"));
		assertTrue(js.contains("/setupcontentqueue"));
		assertTrue(js.contains("/setupreact"));
		assertTrue(js.contains("optional: true"));
		assertTrue(js.contains("REQUIRED_STEPS = 4"));
		assertTrue(js.contains("class': 'tick'"));
		assertTrue(js.contains("localStorage.setItem(CHECKLIST_KEY"));
		assertTrue(js.contains("catch (e)"));
		assertTrue(css.contains(".checklist li"));
	}

	@Test
	void announcementSwitchesAndStatusLineArePresent() throws Exception
	{
		String js = resource("app.js");
		String html = resource("index.html");
		assertTrue(html.contains("<a id=\"announce-set\" href=\"#messages-announce\" data-group=\"announce\""));
		assertTrue(js.contains("announceMoviesOn"));
		assertTrue(js.contains("announceSeasonsOn"));
		assertTrue(js.contains("Announce new movies"));
		assertTrue(js.contains("Announce completed seasons"));
		assertTrue(js.contains("/setupannouncements"));
		assertTrue(js.contains("Announcements: channel set"));
		assertTrue(js.contains("d.announceSet"));
		assertFalse(js.contains("innerHTML"));
	}
}
