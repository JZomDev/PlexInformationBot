package org.example.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** The page has no JS test runner; this checks the status line links and the group ids agree. */
class StatusLinksTest
{
	private static String resource(String name) throws IOException
	{
		try (var in = StatusLinksTest.class.getResourceAsStream("/web/" + name))
		{
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	void statusLineItemsLinkToTheMessageGroups() throws Exception
	{
		String html = resource("index.html");
		String js = resource("app.js");
		for (String id : new String[] {"status", "queue", "role"})
		{
			assertTrue(html.contains("<a id=\"" + id + "-set\" href=\"#messages-" + id + "\""), id);
			assertTrue(js.contains("'messages-' + g.id"));
		}
		assertTrue(js.contains("id: 'messages'"));
	}

	@Test
	void messagesCardIsHiddenUntilALinkOpensOneGroup() throws Exception
	{
		String js = resource("app.js");
		assertFalse(js.contains("id: 'messages', 'class': 'card adv-only'"));
		assertTrue(js.contains("'aria-labelledby': 'h-messages', hidden: ''"));
		assertTrue(js.contains("'aria-labelledby': 'h-' + g.id, hidden: ''"));
		assertTrue(js.contains("$('messages-' + x.id).hidden = x.id !== id"));
		assertTrue(js.contains("id: 'messages-close'"));
		assertTrue(js.contains("ev.key === 'Escape'"));
		assertTrue(js.contains("'aria-expanded'"));
		assertTrue(js.contains("if (!advanced) { closeMessages(false); }"));
		assertTrue(js.contains("$(opener + '-set').focus()"));
	}
}
