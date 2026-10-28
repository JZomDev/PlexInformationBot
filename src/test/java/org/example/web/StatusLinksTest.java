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
		for (String id : new String[] {"status", "queue", "role", "announce"})
		{
			assertTrue(html.contains("<a id=\"" + id + "-set\" href=\"#messages-" + id + "\""), id);
			assertTrue(js.contains("'messages-' + g.id"));
		}
		assertTrue(js.contains("id: 'messages'"));
	}

	@Test
	void announceGroupHasOneChannelFieldAndNoMessageBox() throws Exception
	{
		String js = resource("app.js");
		assertTrue(js.contains("{ id: 'announce', title: 'Announcements channel'"));
		assertTrue(js.contains("{ key: 'announceChannelId', label: 'Announcements channel ID', inputmode: 'numeric' }"));
		assertTrue(js.contains("/setupannouncements"));
		assertTrue(js.contains("Copy Channel ID"));
		assertTrue(js.contains("if (g.messageKey)"));
		assertFalse(js.contains("['status', 'queue', 'role']"));
		assertTrue(js.contains("MESSAGE_GROUPS.forEach(function (g) {\n      var id = g.id;"));
	}

	@Test
	void messagesOpenInAModalDialogOneGroupAtATime() throws Exception
	{
		String js = resource("app.js");
		String css = resource("style.css");
		assertFalse(js.contains("scrollIntoView"));
		assertFalse(js.contains("flash"));
		assertTrue(js.contains("el('dialog', { id: 'messages'"));
		assertTrue(js.contains("dlg.showModal()"));
		assertTrue(js.contains("'aria-labelledby': 'h-messages', hidden: ''"));
		assertTrue(js.contains("$('messages-' + x.id).hidden = x.id !== id"));
		assertTrue(js.contains("$('h-messages').textContent = g.title"));
		assertTrue(js.contains("id: 'messages-close'"));
		assertTrue(js.contains("id: 'messages-save'"));
		assertTrue(js.contains("saveSettings(true)"));
		assertTrue(js.contains("addEventListener('cancel'"));
		assertTrue(js.contains("ev.target === dlg"));
		assertTrue(js.contains("'aria-expanded'"));
		assertTrue(js.contains("if (!advanced) { closeMessages(false); }"));
		assertTrue(js.contains("$(opener + '-set').focus()"));
		assertTrue(css.contains("html.modal-open"));
		assertTrue(css.contains("dialog#messages::backdrop"));
		assertTrue(css.contains("max-height: 90vh"));
	}
}
