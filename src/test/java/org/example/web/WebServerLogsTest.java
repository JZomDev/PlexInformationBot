package org.example.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.bot.BotController;
import org.example.bot.BotState;
import org.example.config.Settings;
import org.example.config.SettingsStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebServerLogsTest
{
	private static final Gson GSON = new Gson();

	@TempDir
	Path tmp;

	private WebServer server;
	private HttpClient http;
	private String sid;

	@BeforeEach
	void setUp() throws Exception
	{
		SettingsStore settings = new SettingsStore(tmp, Map.of("BOT_TOKEN", "BOTSECRET-1234567890"));
		BotController bot = new BotController()
		{
			@Override
			public void restart(Settings effective)
			{
			}

			@Override
			public BotState state()
			{
				return BotState.RUNNING;
			}
		};
		MutableClock clock = new MutableClock();
		CredentialStore credentials = new CredentialStore(tmp);
		server = new WebServer(0, settings, credentials, new SessionManager(clock), new LoginThrottle(clock),
				new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x")), bot,
				(c, m) -> new CheckResult(true, "x"));
		server.start();
		http = HttpClient.newHttpClient();
	}

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	private HttpResponse<String> get(String path, boolean auth) throws Exception
	{
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path));
		if (auth && sid != null)
		{
			b.header("Cookie", "sid=" + sid);
		}
		return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
	}

	private void ready() throws Exception
	{
		HttpRequest login = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/login"))
				.POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"admin\",\"password\":\"admin\"}")).build();
		HttpResponse<String> r = http.send(login, HttpResponse.BodyHandlers.ofString());
		String c = r.headers().firstValue("Set-Cookie").orElseThrow();
		sid = c.substring(4, c.indexOf(';'));
		String csrf = GSON.fromJson(r.body(), JsonObject.class).get("csrf").getAsString();
		HttpRequest change = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/credentials"))
				.header("Cookie", "sid=" + sid).header("X-CSRF-Token", csrf)
				.POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"owner\",\"password\":\"correct horse\"}")).build();
		r = http.send(change, HttpResponse.BodyHandlers.ofString());
		c = r.headers().firstValue("Set-Cookie").orElseThrow();
		sid = c.substring(4, c.indexOf(';'));
	}

	@Test
	void requiresSession() throws Exception
	{
		assertEquals(401, get("/api/logs", false).statusCode());
	}

	@Test
	void noFileGivesEmptyText() throws Exception
	{
		ready();
		HttpResponse<String> r = get("/api/logs", true);
		assertEquals(200, r.statusCode());
		assertEquals("", GSON.fromJson(r.body(), JsonObject.class).get("text").getAsString());
	}

	@Test
	void servesRedactedTailAndIncrements() throws Exception
	{
		ready();
		Path log = tmp.resolve("logs").resolve("bot.log");
		Files.createDirectories(log.getParent());
		Files.writeString(log, "start BOTSECRET-1234567890 ok\nGET /x?X-Plex-Token=plexsecret&a=b\n");
		JsonObject a = GSON.fromJson(get("/api/logs", true).body(), JsonObject.class);
		String text = a.get("text").getAsString();
		assertFalse(text.contains("BOTSECRET"));
		assertFalse(text.contains("plexsecret"));
		assertTrue(text.contains("a=b"));
		assertTrue(a.get("reset").getAsBoolean());
		long next = a.get("nextOffset").getAsLong();
		Files.writeString(log, "more\n", java.nio.file.StandardOpenOption.APPEND);
		JsonObject b = GSON.fromJson(get("/api/logs?offset=" + next, true).body(), JsonObject.class);
		assertEquals("more\n", b.get("text").getAsString());
		assertFalse(b.get("reset").getAsBoolean());
	}

	@Test
	void badOffsetIsTreatedAsReset() throws Exception
	{
		ready();
		Path log = tmp.resolve("logs").resolve("bot.log");
		Files.createDirectories(log.getParent());
		Files.writeString(log, "hello\n");
		JsonObject a = GSON.fromJson(get("/api/logs?offset=abc", true).body(), JsonObject.class);
		assertTrue(a.get("reset").getAsBoolean());
		assertEquals("hello\n", a.get("text").getAsString());
	}
}
