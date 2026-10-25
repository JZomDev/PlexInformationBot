package org.example.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** No JS runner here; this checks the reveal button is wired to every password field and resets. */
class RevealPasswordPageTest
{
	private static String resource(String name) throws IOException
	{
		try (var in = RevealPasswordPageTest.class.getResourceAsStream("/web/" + name))
		{
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	void everyPasswordFieldGetsAnAccessibleRevealToggle() throws Exception
	{
		String js = resource("app.js");
		String css = resource("style.css");
		assertTrue(js.contains("['login-password', 'change-password', 'change-repeat']"));
		assertTrue(js.contains("if (f.secret) { addReveal(inp); }"));
		assertTrue(js.contains("{ type: 'button', 'class': 'reveal' }"));
		assertTrue(js.contains("'Show value'") && js.contains("'Hide value'"));
		assertTrue(js.contains("aria-pressed"));
		assertTrue(js.contains("stroke', 'currentColor'") || js.contains("'stroke'"));
		assertFalse(js.contains("innerHTML"));
		assertTrue(css.contains(".pw-wrap input { padding-right: 48px; }"));
	}

	@Test
	void revealResetsOnViewChangeAndRefill() throws Exception
	{
		String js = resource("app.js");
		int show = js.indexOf("function show(view) {");
		assertTrue(js.substring(show, show + 80).contains("hideAllReveals();"));
		int fill = js.indexOf("function fillSettings(data) {");
		assertTrue(js.substring(fill, fill + 120).contains("hideAllReveals();"));
	}
}
