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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebServerTest
{
	private static final Gson GSON = new Gson();

	@TempDir
	Path tmp;

	private static final class FakeBot implements BotController
	{
		final List<Settings> restarts = new ArrayList<>();

		@Override
		public synchronized void restart(Settings effective)
		{
			restarts.add(effective);
		}

		@Override
		public BotState state()
		{
			return BotState.RUNNING;
		}
	}

	private static final class FakeChecks extends ConnectionChecks
	{
		final List<String> calls = new ArrayList<>();

		FakeChecks()
		{
			super(HttpClient.newHttpClient(), t -> new CheckResult(true, "unused"));
		}

		@Override
		public CheckResult plex(String ip, String port, String token)
		{
			calls.add("plex|" + ip + "|" + port + "|" + token);
			return new CheckResult(true, "Plex fine");
		}

		@Override
		public CheckResult radarr(String url, String key)
		{
			calls.add("radarr|" + url + "|" + key);
			return new CheckResult(false, "Rejected: check the key");
		}

		@Override
		public CheckResult discord(String token)
		{
			calls.add("discord|" + token);
			return new CheckResult(true, "ok");
		}
	}

	private record Res(int status, String body, HttpResponse<String> raw)
	{
		JsonObject json()
		{
			return GSON.fromJson(body, JsonObject.class);
		}

		String header(String name)
		{
			return raw.headers().firstValue(name).orElse(null);
		}
	}

	private static final class Client
	{
		String sid;
		String csrf;
	}

	private SettingsStore settings;
	private CredentialStore credentials;
	private FakeBot bot;
	private FakeChecks checks;
	private ConnectionChecks checksOverride;
	private WebServer server;
	private HttpClient http;
	private Path configDir;

	@BeforeEach
	void setUp() throws IOException
	{
		configDir = tmp;
		start(new SettingsStore(configDir, Map.of()));
	}

	private void start(SettingsStore store) throws IOException
	{
		if (server != null)
		{
			server.stop();
		}
		settings = store;
		credentials = new CredentialStore(configDir);
		bot = new FakeBot();
		checks = new FakeChecks();
		MutableClock clock = new MutableClock();
		server = new WebServer(0, settings, credentials, new SessionManager(clock), new LoginThrottle(clock),
				checksOverride != null ? checksOverride : checks, bot,
					(channelId, messageId) -> new CheckResult(true, "unused"));
		server.start();
		http = HttpClient.newHttpClient();
	}

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	private Res call(String method, String path, String body, Client c, String... headers) throws Exception
	{
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path));
		b.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body));
		if (c != null && c.sid != null)
		{
			b.header("Cookie", "sid=" + c.sid);
		}
		if (c != null && c.csrf != null)
		{
			b.header("X-CSRF-Token", c.csrf);
		}
		for (int i = 0; i < headers.length; i += 2)
		{
			b.header(headers[i], headers[i + 1]);
		}
		HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
		return new Res(r.statusCode(), r.body(), r);
	}

	private static String creds(String user, String pass)
	{
		JsonObject o = new JsonObject();
		o.addProperty("username", user);
		o.addProperty("password", pass);
		return o.toString();
	}

	private Client login(String user, String pass) throws Exception
	{
		Client c = new Client();
		Res r = call("POST", "/api/login", creds(user, pass), null);
		assertEquals(200, r.status(), r.body());
		String cookie = r.header("Set-Cookie");
		c.sid = cookie.substring("sid=".length(), cookie.indexOf(';'));
		c.csrf = call("GET", "/api/session", null, c).json().get("csrf").getAsString();
		return c;
	}

	private Client loggedInReady() throws Exception
	{
		Client c = login("admin", "admin");
		Res r = call("POST", "/api/credentials", creds("owner", "correct horse"), c);
		assertEquals(200, r.status(), r.body());
		String cookie = r.header("Set-Cookie");
		c.sid = cookie.substring("sid=".length(), cookie.indexOf(';'));
		c.csrf = r.json().get("csrf").getAsString();
		return c;
	}

	private static String settingsJson(String botToken, String plexIp)
	{
		JsonObject o = new JsonObject();
		o.addProperty("botToken", botToken);
		o.addProperty("plexIp", plexIp);
		o.addProperty("plexToken", "plex-secret-value");
		return o.toString();
	}

	@Test
	void loginDefaultThenMustChange() throws Exception
	{
		Client c = login("admin", "admin");
		JsonObject s = call("GET", "/api/session", null, c).json();
		assertTrue(s.get("loggedIn").getAsBoolean());
		assertTrue(s.get("mustChange").getAsBoolean());
		assertEquals(403, call("GET", "/api/settings", null, c).status());
		assertEquals(403, call("GET", "/api/status", null, c).status());
		Res changed = call("POST", "/api/credentials", creds("owner", "correct horse"), c);
		assertEquals(200, changed.status());
		c.sid = changed.header("Set-Cookie").substring(4, changed.header("Set-Cookie").indexOf(';'));
		c.csrf = changed.json().get("csrf").getAsString();
		assertFalse(call("GET", "/api/session", null, c).json().get("mustChange").getAsBoolean());
		assertEquals(200, call("GET", "/api/settings", null, c).status());
		assertEquals(200, call("GET", "/api/status", null, c).status());
	}

	@Test
	void weakCredentialChangeIs400() throws Exception
	{
		Client c = login("admin", "admin");
		Res r = call("POST", "/api/credentials", creds("owner", "admin"), c);
		assertEquals(400, r.status());
		assertTrue(credentials.mustChange());
		assertEquals(400, call("POST", "/api/credentials", creds("owner", "short"), c).status());
	}

	@Test
	void badPasswordIs401AndSixthAttemptIs429() throws Exception
	{
		for (int i = 0; i < 5; i++)
		{
			Res r = call("POST", "/api/login", creds("admin", "wrong" + i), null);
			assertEquals(401, r.status());
			assertFalse(r.body().contains("wrong"));
		}
		assertEquals(429, call("POST", "/api/login", creds("admin", "admin"), null).status());
	}

	@Test
	void forwardedForDoesNotEscapeThrottle() throws Exception
	{
		for (int i = 0; i < 5; i++)
		{
			call("POST", "/api/login", creds("admin", "bad"), null, "X-Forwarded-For", "10.0.0." + i);
		}
		assertEquals(429, call("POST", "/api/login", creds("admin", "admin"), null,
				"X-Forwarded-For", "10.9.9.9").status());
	}

	@Test
	void stateChangingRequestWithoutCsrfIs403() throws Exception
	{
		Client c = loggedInReady();
		Client noCsrf = new Client();
		noCsrf.sid = c.sid;
		assertEquals(403, call("PUT", "/api/settings", settingsJson("", "1.2.3.4"), noCsrf).status());
		assertEquals(403, call("POST", "/api/logout", null, noCsrf).status());
		assertEquals(403, call("POST", "/api/credentials", creds("x", "another pass1"), noCsrf).status());
		Client wrong = new Client();
		wrong.sid = c.sid;
		wrong.csrf = "nope";
		assertEquals(403, call("PUT", "/api/settings", settingsJson("", "1.2.3.4"), wrong).status());
		assertTrue(bot.restarts.isEmpty());
	}

	@Test
	void settingsRequireSession() throws Exception
	{
		assertEquals(401, call("GET", "/api/settings", null, null).status());
		assertEquals(401, call("PUT", "/api/settings", settingsJson("", "1.2.3.4"), null).status());
		assertEquals(401, call("GET", "/api/status", null, null).status());
	}

	@Test
	void crossOriginLoginIs403() throws Exception
	{
		Res r = call("POST", "/api/login", creds("admin", "admin"), null, "Origin", "http://evil.example");
		assertEquals(403, r.status());
		assertNull(r.header("Set-Cookie"));
		String same = "http://127.0.0.1:" + server.port();
		assertEquals(200, call("POST", "/api/login", creds("admin", "admin"), null, "Origin", same).status());
		assertEquals(403, call("POST", "/api/login", creds("admin", "admin"), null, "Origin", "null").status());
	}

	@Test
	void putSettingsKeepsMaskedSecretAndRestartsBot() throws Exception
	{
		Client c = loggedInReady();
		assertEquals(200, call("PUT", "/api/settings", settingsJson("real-bot-token", "1.2.3.4"), c).status());
		assertEquals(1, bot.restarts.size());
		Res again = call("PUT", "/api/settings", settingsJson(Settings.MASK, "5.6.7.8"), c);
		assertEquals(200, again.status());
		assertEquals(2, bot.restarts.size());
		assertEquals("real-bot-token", settings.fileValues().botToken());
		assertEquals("5.6.7.8", settings.effective().plexIp());
		assertEquals("real-bot-token", bot.restarts.get(1).botToken());
		assertFalse(again.body().contains("real-bot-token"));
	}

	@Test
	void putSettingsIOExceptionReturns500WithoutSecrets() throws Exception
	{
		Client c = loggedInReady();
		// make settings.json unwritable by occupying its tmp name with a non-empty directory
		Path blocker = configDir.resolve("settings.json.tmp");
		Files.createDirectories(blocker.resolve("x"));
		Res r = call("PUT", "/api/settings", settingsJson("super-secret-token", "1.2.3.4"), c);
		assertEquals(500, r.status());
		assertEquals("Could not save settings", r.json().get("error").getAsString());
		assertFalse(r.body().contains("super-secret-token"));
		assertFalse(r.body().contains(configDir.toString()));
		assertTrue(bot.restarts.isEmpty());
	}

	@Test
	void corruptSettingsFileStillServesLoginAndShowsWarning() throws Exception
	{
		Files.writeString(configDir.resolve("settings.json"), "{ not json");
		start(new SettingsStore(configDir, Map.of()));
		Client c = loggedInReady();
		Res r = call("GET", "/api/settings", null, c);
		assertEquals(200, r.status());
		assertFalse(r.json().get("warning").isJsonNull());
		assertTrue(r.json().get("warning").getAsString().contains("settings.json"));
	}

	@Test
	void cookieIsHttpOnlySameSiteStrictAndSecureOnlyBehindHttpsProxyHeader() throws Exception
	{
		Res plain = call("POST", "/api/login", creds("admin", "admin"), null);
		String cookie = plain.header("Set-Cookie");
		assertTrue(cookie.startsWith("sid="));
		assertTrue(cookie.contains("HttpOnly"));
		assertTrue(cookie.contains("SameSite=Strict"));
		assertTrue(cookie.contains("Path=/"));
		assertFalse(cookie.contains("Secure"));
		Res proxied = call("POST", "/api/login", creds("admin", "admin"), null, "X-Forwarded-Proto", "https");
		assertTrue(proxied.header("Set-Cookie").contains("; Secure"));
		Res spoof = call("POST", "/api/login", creds("admin", "admin"), null, "X-Forwarded-Proto", "http");
		assertFalse(spoof.header("Set-Cookie").contains("Secure"));
	}

	@Test
	void getSettingsNeverContainsFullSecret() throws Exception
	{
		Client c = loggedInReady();
		call("PUT", "/api/settings", settingsJson("real-bot-token", "1.2.3.4"), c);
		Res r = call("GET", "/api/settings", null, c);
		assertEquals(200, r.status());
		assertFalse(r.body().contains("real-bot-token"));
		assertFalse(r.body().contains("plex-secret-value"));
		assertEquals(Settings.MASK, r.json().get("botToken").getAsString());
		assertEquals("1.2.3.4", r.json().get("plexIp").getAsString());
	}

	private static String keyBody(String key)
	{
		JsonObject o = new JsonObject();
		o.addProperty("key", key);
		return o.toString();
	}

	@Test
	void secretRouteRevealsSavedAndEnvValuesOnly() throws Exception
	{
		start(new SettingsStore(configDir, Map.of("RADARR_API", "env-radarr")));
		Client c = loggedInReady();
		call("PUT", "/api/settings", settingsJson("real-bot-token", "1.2.3.4"), c);
		Res r = call("POST", "/api/secret", keyBody("botToken"), c);
		assertEquals(200, r.status(), r.body());
		assertEquals("real-bot-token", r.json().get("value").getAsString());
		assertEquals("no-store", r.header("Cache-Control"));
		assertEquals("plex-secret-value",
				call("POST", "/api/secret", keyBody("plexToken"), c).json().get("value").getAsString());
		assertEquals("env-radarr",
				call("POST", "/api/secret", keyBody("radarrApiKey"), c).json().get("value").getAsString());
		assertFalse(call("GET", "/api/settings", null, c).body().contains("real-bot-token"));
	}

	@Test
	void secretRouteRejectsBadRequests() throws Exception
	{
		Client c = loggedInReady();
		Res unknown = call("POST", "/api/secret", keyBody("plexIp<script>"), c);
		assertEquals(400, unknown.status());
		assertFalse(unknown.body().contains("script"));
		assertEquals(400, call("POST", "/api/secret", "{oops", c).status());
		assertEquals(400, call("POST", "/api/secret", "{}", c).status());
		Client noCsrf = new Client();
		noCsrf.sid = c.sid;
		assertEquals(403, call("POST", "/api/secret", keyBody("botToken"), noCsrf).status());
		assertEquals(401, call("POST", "/api/secret", keyBody("botToken"), null).status());
	}

	@Test
	void envSecretsAreMaskedToo() throws Exception
	{
		start(new SettingsStore(configDir, Map.of("BOT_TOKEN", "env-bot-token")));
		Client c = loggedInReady();
		Res r = call("GET", "/api/settings", null, c);
		assertFalse(r.body().contains("env-bot-token"));
		assertEquals(Settings.MASK, r.json().get("botToken").getAsString());
	}

	@Test
	void statusReportsState() throws Exception
	{
		Client c = loggedInReady();
		JsonObject s = call("GET", "/api/status", null, c).json();
		assertEquals("RUNNING", s.get("botState").getAsString());
		assertFalse(s.get("queueSet").getAsBoolean());
		assertFalse(s.get("statusSet").getAsBoolean());
		assertFalse(s.get("roleSet").getAsBoolean());
		assertFalse(s.get("announceSet").getAsBoolean());
	}

	@Test
	void statusReportsAnnouncementChannelOnceSet() throws Exception
	{
		settings.update(saved -> new Settings(saved.botToken(), saved.plexIp(), saved.plexPort(), saved.plexToken(),
				saved.tautulliUrl(), saved.tautulliApiKey(), saved.radarrUrl(), saved.radarrApiKey(),
				saved.sonarrUrl(), saved.sonarrApiKey(), saved.roleId(), saved.roleChannelId(), saved.roleMessageId(),
				saved.statusChannelId(), saved.statusMessageId(), saved.queueChannelId(), saved.queueMessageId(),
				"123456789012345678", "true", "true"));
		Client c = loggedInReady();
		JsonObject s = call("GET", "/api/status", null, c).json();
		assertTrue(s.get("announceSet").getAsBoolean());
	}

	@Test
	void logoutDestroysSession() throws Exception
	{
		Client c = loggedInReady();
		assertEquals(200, call("POST", "/api/logout", null, c).status());
		assertEquals(401, call("GET", "/api/settings", null, c).status());
		assertFalse(call("GET", "/api/session", null, c).json().get("loggedIn").getAsBoolean());
	}

	@Test
	void oversizedBodyIs413() throws Exception
	{
		String big = "{\"username\":\"" + "a".repeat(70_000) + "\"}";
		assertEquals(413, call("POST", "/api/login", big, null).status());
	}

	@Test
	void malformedJsonIs400() throws Exception
	{
		Res r = call("POST", "/api/login", "{oops", null);
		assertEquals(400, r.status());
		assertEquals(400, call("POST", "/api/login", "null", null).status());
	}

	@Test
	void unknownRouteIs404AndWrongMethodIs405() throws Exception
	{
		assertEquals(404, call("GET", "/api/nothing", null, null).status());
		Res r = call("GET", "/api/login", null, null);
		assertEquals(405, r.status());
		assertEquals(405, call("DELETE", "/api/session", null, null).status());
		assertEquals(405, call("POST", "/index.html", "x", null).status());
	}

	@Test
	void securityHeadersPresent() throws Exception
	{
		Res api = call("GET", "/api/session", null, null);
		assertEquals("no-store", api.header("Cache-Control"));
		assertEquals("nosniff", api.header("X-Content-Type-Options"));
		Res page = call("GET", "/", null, null);
		assertEquals("nosniff", page.header("X-Content-Type-Options"));
	}

	@Test
	void staticIndexServedAtRootAndMustChangeDoesNotBlockIt() throws Exception
	{
		Res r = call("GET", "/", null, null);
		assertEquals(200, r.status());
		assertTrue(r.body().contains("<html"));
		assertTrue(r.header("Content-Type").startsWith("text/html"));
		assertEquals(200, call("GET", "/index.html", null, null).status());
		assertEquals(404, call("GET", "/missing.js", null, null).status());
	}

	@Test
	void credentialChangeKillsOtherSessionsAndKeepsCaller() throws Exception
	{
		Client a = login("admin", "admin");
		Client b = login("admin", "admin");
		Res r = call("POST", "/api/credentials", creds("owner", "correct horse"), a);
		assertEquals(200, r.status());
		String cookie = r.header("Set-Cookie");
		assertTrue(cookie.contains("HttpOnly") && cookie.contains("SameSite=Strict"));
		a.sid = cookie.substring("sid=".length(), cookie.indexOf(';'));
		a.csrf = r.json().get("csrf").getAsString();
		assertEquals(401, call("GET", "/api/settings", null, b).status());
		assertEquals(200, call("GET", "/api/settings", null, a).status());
		assertEquals(200, call("PUT", "/api/settings", settingsJson("", "1.2.3.4"), a).status());
	}

	@Test
	void putSettingsNormalisesHostFields() throws Exception
	{
		Client c = loggedInReady();
		assertEquals(200, call("PUT", "/api/settings", "{\"radarrUrl\":\"http://192.168.1.10:7878/\"}", c).status());
		assertEquals("192.168.1.10:7878", settings.fileValues().radarrUrl());
		assertTrue(call("GET", "/api/settings", null, c).body().contains("\"radarrUrl\":\"192.168.1.10:7878\""));
	}

	@Test
	void putSettingsKeepsAbsentFieldsAndEmptyStringClears() throws Exception
	{
		Client c = loggedInReady();
		JsonObject full = new JsonObject();
		full.addProperty("radarrApiKey", "radarr-key");
		full.addProperty("tautulliApiKey", "tautulli-key");
		full.addProperty("queueMessageId", "123456789012345678");
		full.addProperty("plexIp", "1.1.1.1");
		assertEquals(200, call("PUT", "/api/settings", full.toString(), c).status());
		assertEquals(200, call("PUT", "/api/settings", "{\"plexIp\":\"2.2.2.2\"}", c).status());
		assertEquals("2.2.2.2", settings.fileValues().plexIp());
		assertEquals("radarr-key", settings.fileValues().radarrApiKey());
		assertEquals("tautulli-key", settings.fileValues().tautulliApiKey());
		assertEquals("123456789012345678", settings.fileValues().queueMessageId());
		assertEquals(200, call("PUT", "/api/settings", "{\"radarrApiKey\":\"\"}", c).status());
		assertEquals("", settings.fileValues().radarrApiKey());
		assertEquals("tautulli-key", settings.fileValues().tautulliApiKey());
		assertEquals(400, call("PUT", "/api/settings", "[1]", c).status());
	}

	private String rawGet(String target) throws IOException
	{
		try (Socket s = new Socket("127.0.0.1", server.port()))
		{
			s.getOutputStream().write(("GET " + target + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n")
					.getBytes(StandardCharsets.US_ASCII));
			s.getOutputStream().flush();
			InputStream in = s.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			in.transferTo(out);
			return out.toString(StandardCharsets.UTF_8);
		}
	}

	@Test
	void pathTraversalIs404() throws Exception
	{
		String[] targets = {
				"/../log4j2.xml", "/..%2flog4j2.xml", "/%2e%2e/log4j2.xml", "/%2E%2E%2Flog4j2.xml",
				"/..%5clog4j2.xml", "/web/../log4j2.xml", "/../../org/example/web/WebServer.class",
				"/a/../../log4j2.xml", "/%252e%252e/log4j2.xml", "/log4j2.xml", "/..;/log4j2.xml",
				"/%00index.html", "/.%2e/log4j2.xml", "//log4j2.xml", "/org/example/web/WebServer.class",
				"/..\\log4j2.xml"
		};
		for (String t : targets)
		{
			String resp = rawGet(t);
			assertFalse(resp.contains("Configuration"), t + " leaked a resource");
			assertFalse(resp.startsWith("HTTP/1.1 200"), t + " returned 200: " + resp);
		}
	}

	@Test
	void testRouteResolvesMaskedSecretsFromSavedSettings() throws Exception
	{
		start(new SettingsStore(configDir, Map.of("PLEX_KEY", "env-plex-key", "RADARR_API", "env-radarr",
				"IP", "1.2.3.4", "PORT", "32400", "RADARR_URL", "h:1")));
		Client c = loggedInReady();
		Res r = call("POST", "/api/test/plex", "{\"plexIp\":\"1.2.3.4\",\"plexPort\":\"32400\",\"plexToken\":\"********\"}", c);
		assertEquals(200, r.status(), r.body());
		assertTrue(r.json().get("ok").getAsBoolean());
		assertEquals("Plex fine", r.json().get("message").getAsString());
		assertEquals("plex|1.2.3.4|32400|env-plex-key", checks.calls.get(0));
		r = call("POST", "/api/test/radarr", "{\"radarrUrl\":\"h:1\"}", c);
		assertEquals(200, r.status());
		assertFalse(r.json().get("ok").getAsBoolean());
		assertEquals("radarr|h:1|env-radarr", checks.calls.get(1));
		call("POST", "/api/test/discord", "{\"botToken\":\"typed-token\"}", c);
		assertEquals("discord|typed-token", checks.calls.get(2));
	}

	@Test
	void testRouteUnknownSectionIs404AndNeedsSessionAndCsrf() throws Exception
	{
		Client c = loggedInReady();
		assertEquals(404, call("POST", "/api/test/bogus", "{}", c).status());
		assertEquals(401, call("POST", "/api/test/plex", "{}", null).status());
		Client noCsrf = new Client();
		noCsrf.sid = c.sid;
		assertEquals(403, call("POST", "/api/test/plex", "{}", noCsrf).status());
		assertEquals(405, call("GET", "/api/test/plex", null, c).status());
		assertTrue(checks.calls.isEmpty());
	}

	@Test
	void testRouteIsBlockedUntilCredentialsChanged() throws Exception
	{
		Client c = login("admin", "admin");
		assertEquals(403, call("POST", "/api/test/plex", "{}", c).status());
		assertTrue(checks.calls.isEmpty());
	}

	@Test
	void testRouteResponseNeverContainsSavedSecret() throws Exception
	{
		com.sun.net.httpserver.HttpServer fake = com.sun.net.httpserver.HttpServer.create(
				new java.net.InetSocketAddress("127.0.0.1", 0), 0);
		fake.createContext("/", ex ->
		{
			byte[] b = "{\"MediaContainer\":{\"friendlyName\":\"srv-SECRET123\"}}".getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(200, b.length);
			ex.getResponseBody().write(b);
			ex.close();
		});
		fake.start();
		try
		{
			checksOverride = new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x"));
			start(new SettingsStore(configDir, Map.of("PLEX_KEY", "SECRET123", "IP", "127.0.0.1",
					"PORT", String.valueOf(fake.getAddress().getPort()))));
			Client c = loggedInReady();
			Res r = call("POST", "/api/test/plex", "{\"plexIp\":\"127.0.0.1\",\"plexPort\":\""
					+ fake.getAddress().getPort() + "\",\"plexToken\":\"********\"}", c);
			assertEquals(200, r.status(), r.body());
			assertTrue(r.json().get("ok").getAsBoolean(), r.body());
			assertFalse(r.body().contains("SECRET123"), r.body());
		}
		finally
		{
			fake.stop(0);
		}
	}

	private static final class Stub
	{
		final com.sun.net.httpserver.HttpServer server;
		final java.util.concurrent.atomic.AtomicInteger hits = new java.util.concurrent.atomic.AtomicInteger();
		final List<String> seen = new java.util.concurrent.CopyOnWriteArrayList<>();

		Stub() throws IOException
		{
			server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
			server.createContext("/", ex ->
			{
				hits.incrementAndGet();
				seen.add(ex.getRequestURI() + " " + ex.getRequestHeaders().getFirst("X-Api-Key"));
				byte[] b = "{}".getBytes(StandardCharsets.UTF_8);
				ex.sendResponseHeaders(200, b.length);
				ex.getResponseBody().write(b);
				ex.close();
			});
			server.start();
		}

		String addr()
		{
			return "127.0.0.1:" + server.getAddress().getPort();
		}
	}

	private static final String READDRESS = "Re-enter the key/token to test a different address";

	private Client realChecksClient(Map<String, String> env) throws Exception
	{
		checksOverride = new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x"));
		start(new SettingsStore(configDir, env));
		return loggedInReady();
	}

	@Test
	void changedHostWithMaskIsNotTestedAndNeverReachesTheHost() throws Exception
	{
		Stub stub = new Stub();
		try
		{
			Client c = realChecksClient(Map.of("RADARR_URL", "saved.invalid:7878", "RADARR_API", "saved-key"));
			Res r = call("POST", "/api/test/radarr", "{\"radarrUrl\":\"" + stub.addr() + "\",\"radarrApiKey\":\"********\"}", c);
			assertEquals(200, r.status(), r.body());
			assertFalse(r.json().get("ok").getAsBoolean());
			assertEquals(READDRESS, r.json().get("message").getAsString());
			r = call("POST", "/api/test/radarr", "{\"radarrUrl\":\"" + stub.addr() + "\"}", c);
			assertEquals(READDRESS, r.json().get("message").getAsString());
			assertEquals(0, stub.hits.get());
		}
		finally
		{
			stub.server.stop(0);
		}
	}

	@Test
	void sameHostWithMaskReachesHostWithSavedSecret() throws Exception
	{
		Stub stub = new Stub();
		try
		{
			Client c = realChecksClient(Map.of("RADARR_URL", stub.addr(), "RADARR_API", "saved-key"));
			call("POST", "/api/test/radarr", "{\"radarrUrl\":\"" + stub.addr() + "\",\"radarrApiKey\":\"********\"}", c);
			assertEquals(1, stub.hits.get());
			assertTrue(stub.seen.get(0).endsWith(" saved-key"), stub.seen.get(0));
		}
		finally
		{
			stub.server.stop(0);
		}
	}

	@Test
	void changedHostWithNewlyTypedSecretReachesHost() throws Exception
	{
		Stub stub = new Stub();
		try
		{
			Client c = realChecksClient(Map.of("RADARR_URL", "saved.invalid:7878", "RADARR_API", "saved-key"));
			call("POST", "/api/test/radarr", "{\"radarrUrl\":\"" + stub.addr() + "\",\"radarrApiKey\":\"typed-key\"}", c);
			assertEquals(1, stub.hits.get());
			assertTrue(stub.seen.get(0).endsWith(" typed-key"), stub.seen.get(0));
		}
		finally
		{
			stub.server.stop(0);
		}
	}

	@Test
	void hostDifferingOnlyBySchemeCaseOrSlashCountsAsSame() throws Exception
	{
		Stub stub = new Stub();
		try
		{
			Client c = realChecksClient(Map.of("RADARR_URL", stub.addr(), "RADARR_API", "saved-key"));
			call("POST", "/api/test/radarr", "{\"radarrUrl\":\" HTTP://" + stub.addr().toUpperCase() + "// \",\"radarrApiKey\":\"********\"}", c);
			assertEquals(1, stub.hits.get());
			assertTrue(stub.seen.get(0).endsWith(" saved-key"), stub.seen.get(0));
		}
		finally
		{
			stub.server.stop(0);
		}
	}

	@Test
	void plexPortChangeWithMaskDoesNotResolve() throws Exception
	{
		Stub stub = new Stub();
		try
		{
			Client c = realChecksClient(Map.of("IP", "127.0.0.1", "PORT", "32400", "PLEX_KEY", "saved-token"));
			int other = stub.server.getAddress().getPort();
			Res r = call("POST", "/api/test/plex", "{\"plexIp\":\"127.0.0.1\",\"plexPort\":\"" + other
					+ "\",\"plexToken\":\"********\"}", c);
			assertEquals(READDRESS, r.json().get("message").getAsString());
			assertEquals(0, stub.hits.get());
		}
		finally
		{
			stub.server.stop(0);
		}
	}

	@Test
	void failureLineHasClassAndFolderButNeverTheMessage()
	{
		String line = WebServer.failureLine("Could not save settings",
				new IOException("secret-token-123 /etc/passwd"), Path.of("/config"));
		assertTrue(line.contains("java.io.IOException"), line);
		assertTrue(line.contains("/config"), line);
		assertFalse(line.contains("secret-token-123"), line);
	}
}
