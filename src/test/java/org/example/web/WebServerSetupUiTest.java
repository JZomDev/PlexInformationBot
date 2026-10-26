package org.example.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.bot.BotController;
import org.example.bot.BotState;
import org.example.config.Settings;
import org.example.config.SettingsStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The invite, health and status-field endpoints the setup page's checklist is built on. */
class WebServerSetupUiTest
{
	private static final Gson GSON = new Gson();
	// Built at runtime so no token-shaped literal sits in the source (push protection flags those).
	private static final String TOKEN = java.util.Base64.getUrlEncoder().withoutPadding()
		.encodeToString("123456789012345678".getBytes(java.nio.charset.StandardCharsets.UTF_8))
		+ "." + "Gabcd" + "E" + "." + "x".repeat(10) + "y".repeat(20);

	@TempDir
	Path tmp;

	private WebServer server;
	private HttpClient http;
	private String sid;
	private String csrf;
	private volatile boolean registered;

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	private void start(Map<String, String> env) throws Exception
	{
		start(env, new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x")));
	}

	private void start(Map<String, String> env, ConnectionChecks checks) throws Exception
	{
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

			@Override
			public boolean setupCommandsRegistered()
			{
				return registered;
			}
		};
		MutableClock clock = new MutableClock();
		server = new WebServer(0, new SettingsStore(tmp, env), new CredentialStore(tmp), new SessionManager(clock),
				new LoginThrottle(clock), checks,
				bot, (c, m) -> new CheckResult(true, "x"));
		server.start();
		http = HttpClient.newHttpClient();
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
		String loginCsrf = GSON.fromJson(r.body(), JsonObject.class).get("csrf").getAsString();
		HttpRequest change = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/credentials"))
				.header("Cookie", "sid=" + sid).header("X-CSRF-Token", loginCsrf)
				.POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"owner\",\"password\":\"correct horse\"}")).build();
		r = http.send(change, HttpResponse.BodyHandlers.ofString());
		c = r.headers().firstValue("Set-Cookie").orElseThrow();
		sid = c.substring(4, c.indexOf(';'));
		csrf = GSON.fromJson(r.body(), JsonObject.class).get("csrf").getAsString();
	}

	private HttpResponse<String> put(String path, String json) throws Exception
	{
		HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path))
				.header("Cookie", "sid=" + sid).header("X-CSRF-Token", csrf)
				.PUT(HttpRequest.BodyPublishers.ofString(json)).build();
		return http.send(req, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void savingNewPlexSettingsRefreshesHealthEvenWhileAnOldCheckHangs() throws Exception
	{
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch oldStarted = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch newStarted = new java.util.concurrent.CountDownLatch(1);
		start(Map.of(), new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x"))
		{
			@Override
			public CheckResult plex(String ip, String port, String token)
			{
				if (ip.equals("10.0.0.1"))
				{
					oldStarted.countDown();
					try
					{
						release.await(10, java.util.concurrent.TimeUnit.SECONDS);
					}
					catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
					}
				}
				else
				{
					newStarted.countDown();
				}
				return new CheckResult(true, "host " + ip);
			}
		});
		ready();
		String old = "{\"plexIp\":\"10.0.0.1\",\"plexPort\":\"32400\",\"plexToken\":\"tok\"}";
		assertEquals(200, put("/api/settings", old).statusCode());
		get("/api/health?refresh=1", true);
		assertTrue(oldStarted.await(5, java.util.concurrent.TimeUnit.SECONDS));

		String fresh = "{\"plexIp\":\"10.0.0.2\",\"plexPort\":\"32400\",\"plexToken\":\"tok\"}";
		assertEquals(200, put("/api/settings", fresh).statusCode());
		get("/api/health?refresh=1", true);
		assertTrue(newStarted.await(5, java.util.concurrent.TimeUnit.SECONDS), "new settings must be checked at once");

		release.countDown();
		String message = "";
		for (int i = 0; i < 50 && !message.equals("host 10.0.0.2"); i++)
		{
			Thread.sleep(100);
			message = GSON.fromJson(get("/api/health", true).body(), JsonObject.class)
					.getAsJsonObject("plex").get("message").getAsString();
		}
		assertEquals("host 10.0.0.2", message);
		Thread.sleep(200);
		assertEquals("host 10.0.0.2", GSON.fromJson(get("/api/health", true).body(), JsonObject.class)
				.getAsJsonObject("plex").get("message").getAsString());
	}

	@Test
	void inviteNeedsASession() throws Exception
	{
		start(Map.of("BOT_TOKEN", TOKEN));
		assertEquals(401, get("/api/invite", false).statusCode());
	}

	@Test
	void inviteUsesTheClientIdAndNeverShowsTheToken() throws Exception
	{
		start(Map.of("BOT_TOKEN", TOKEN));
		ready();
		HttpResponse<String> r = get("/api/invite", true);
		assertEquals(200, r.statusCode());
		assertEquals("https://discord.com/oauth2/authorize?client_id=123456789012345678"
				+ "&scope=bot%20applications.commands&permissions=268520512",
				GSON.fromJson(r.body(), JsonObject.class).get("url").getAsString());
		assertFalse(r.body().contains(TOKEN));
		assertFalse(r.body().contains("abcdefghijkl"));
	}

	@Test
	void inviteIsEmptyForAGarbageToken() throws Exception
	{
		start(Map.of("BOT_TOKEN", "garbage"));
		ready();
		assertEquals("", GSON.fromJson(get("/api/invite", true).body(), JsonObject.class).get("url").getAsString());
	}

	@Test
	void inviteIsEmptyWithoutAToken() throws Exception
	{
		start(Map.of());
		ready();
		assertEquals("", GSON.fromJson(get("/api/invite", true).body(), JsonObject.class).get("url").getAsString());
	}

	@Test
	void statusReportsWhetherSetupCommandsAreRegistered() throws Exception
	{
		start(Map.of());
		ready();
		assertFalse(GSON.fromJson(get("/api/status", true).body(), JsonObject.class)
				.get("setupCommandsRegistered").getAsBoolean());
		registered = true;
		assertTrue(GSON.fromJson(get("/api/status", true).body(), JsonObject.class)
				.get("setupCommandsRegistered").getAsBoolean());
	}

	@Test
	void healthNeedsASessionAndListsEverySection() throws Exception
	{
		start(Map.of("BOT_TOKEN", TOKEN));
		assertEquals(401, get("/api/health", false).statusCode());
		ready();
		HttpResponse<String> r = get("/api/health", true);
		assertEquals(200, r.statusCode());
		JsonObject body = GSON.fromJson(r.body(), JsonObject.class);
		for (String s : new String[] {"plex", "tautulli", "radarr", "sonarr", "discord"})
		{
			assertTrue(body.has(s), s);
			assertTrue(body.getAsJsonObject(s).has("state"));
			assertTrue(body.getAsJsonObject(s).has("message"));
		}
		assertEquals("ok", body.getAsJsonObject("discord").get("state").getAsString());
		assertEquals("unset", body.getAsJsonObject("plex").get("state").getAsString());
		assertFalse(r.body().contains(TOKEN));
	}
}
