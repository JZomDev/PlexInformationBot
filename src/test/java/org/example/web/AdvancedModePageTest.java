package org.example.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** No JS runner here; this checks the page wires the toggle and the log view to the same ids. */
class AdvancedModePageTest
{
	private static String resource(String name) throws IOException
	{
		try (var in = AdvancedModePageTest.class.getResourceAsStream("/web/" + name))
		{
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	void toggleIsALabelledCheckboxRememberedInTryCatch() throws Exception
	{
		String html = resource("index.html");
		String js = resource("app.js");
		assertTrue(html.contains("<input id=\"advanced\" type=\"checkbox\"> Advanced mode"));
		assertTrue(js.contains("try { advanced = localStorage.getItem('advancedMode')"));
		assertTrue(js.contains("a.removeAttribute('href')"));
		assertTrue(js.contains("' adv-only'"));
	}

	@Test
	void logViewPollsOffsetEndpointAndStopsWhenHidden() throws Exception
	{
		String html = resource("index.html");
		String js = resource("app.js");
		assertTrue(html.contains("id=\"logs-toggle\""));
		assertTrue(html.contains("id=\"logs-pause\""));
		assertTrue(js.contains("'/api/logs?offset=' + logOffset"));
		assertTrue(js.contains("document.hidden"));
		assertTrue(js.contains("LOG_MAX_LINES = 1000"));
	}
}
