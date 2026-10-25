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
}
