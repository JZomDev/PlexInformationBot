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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebServerMessagesTest
{
	private static final Gson GSON = new Gson();
	private static final String C1 = "111111111111111111";
	private static final String M1 = "222222222222222222";
	private static final String R1 = "333333333333333333";

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

	private static final class FakeProbe implements MessageProbe
	{
		final List<String> calls = new ArrayList<>();
		CheckResult result = new CheckResult(true, "Found a message from Bot in #general; the bot can edit it");
		CheckResult roleResult = new CheckResult(true, "unused");

		@Override
		public CheckResult probe(String channelId, String messageId)
		{
			calls.add("message|" + channelId + "|" + messageId);
			return result;
		}

		@Override
		public CheckResult probeRole(String roleId, String channelId)
		{
			calls.add("role|" + roleId + "|" + channelId);
			return roleResult;
		}
	}

	private record Res(int status, String body)
	{
		JsonObject json()
		{
			return GSON.fromJson(body, JsonObject.class);
		}
	}

	private SettingsStore settings;
	private FakeBot bot;
	private FakeProbe probe;
	private WebServer server;
	private HttpClient http;
	private String sid;
	private String csrf;

	@BeforeEach
	void setUp() throws Exception
	{
		start(Map.of());
	}

	private void start(Map<String, String> env) throws Exception
	{
		if (server != null)
		{
			server.stop();
		}
		settings = new SettingsStore(tmp, env);
		bot = new FakeBot();
		probe = new FakeProbe();
		MutableClock clock = new MutableClock();
		ConnectionChecks checks = new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x"));
		server = new WebServer(0, settings, new CredentialStore(tmp), new SessionManager(clock),
				new LoginThrottle(clock), checks, bot, probe);
		server.start();
		http = HttpClient.newHttpClient();
		sid = null;
		csrf = null;
	}

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	private Res call(String method, String path, String body, boolean withSid, boolean withCsrf) throws Exception
	{
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path));
		b.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body));
		if (withSid && sid != null)
		{
			b.header("Cookie", "sid=" + sid);
		}
		if (withCsrf && csrf != null)
		{
			b.header("X-CSRF-Token", csrf);
		}
		HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
		return new Res(r.statusCode(), r.body());
	}

	private Res call(String method, String path, String body) throws Exception
	{
		return call(method, path, body, true, true);
	}

	private void login(String user, String pass) throws Exception
	{
		HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/login"))
				.POST(HttpRequest.BodyPublishers.ofString(
						"{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}")).build();
		HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, r.statusCode());
		String cookie = r.headers().firstValue("Set-Cookie").orElseThrow();
		sid = cookie.substring("sid=".length(), cookie.indexOf(';'));
		csrf = GSON.fromJson(r.body(), JsonObject.class).get("csrf").getAsString();
	}

	private void ready() throws Exception
	{
		login("admin", "admin");
		HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/credentials"))
				.header("Cookie", "sid=" + sid).header("X-CSRF-Token", csrf)
				.POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"owner\",\"password\":\"correct horse\"}"))
				.build();
		HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, r.statusCode());
		String cookie = r.headers().firstValue("Set-Cookie").orElseThrow();
		sid = cookie.substring("sid=".length(), cookie.indexOf(';'));
		csrf = GSON.fromJson(r.body(), JsonObject.class).get("csrf").getAsString();
	}

	private static String ids(String... kv)
	{
		JsonObject o = new JsonObject();
		for (int i = 0; i < kv.length; i += 2)
		{
			o.addProperty(kv[i], kv[i + 1]);
		}
		return o.toString();
	}

	@Test
	void statusTestPassesTypedIdsAndReportsTheProbeResult() throws Exception
	{
		ready();
		Res r = call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", M1));
		assertEquals(200, r.status(), r.body());
		assertTrue(r.json().get("ok").getAsBoolean());
		assertEquals("Found a message from Bot in #general; the bot can edit it", r.json().get("message").getAsString());
		assertEquals(List.of("message|" + C1 + "|" + M1), probe.calls);
	}

	@Test
	void queueTestUsesTheQueueFields() throws Exception
	{
		ready();
		call("POST", "/api/test/queue", ids("queueChannelId", C1, "queueMessageId", M1, "statusChannelId", "5"));
		assertEquals(List.of("message|" + C1 + "|" + M1), probe.calls);
	}

	@Test
	void notConnectedNotFoundAndNotByTheBotAreReportedAsGiven() throws Exception
	{
		ready();
		for (String text : new String[] {"The bot is not connected yet. Save the Discord token first.",
				"That message was written by someone else, so the bot cannot edit it"})
		{
			probe.result = new CheckResult(false, text);
			Res r = call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", M1));
			assertEquals(200, r.status());
			assertFalse(r.json().get("ok").getAsBoolean());
			assertEquals(text, r.json().get("message").getAsString());
		}
	}

	@Test
	void probeRuntimeFailureBecomesAFixedMessage() throws Exception
	{
		ready();
		MessageProbe boom = new MessageProbe()
		{
			@Override
			public CheckResult probe(String channelId, String messageId)
			{
				throw new IllegalStateException("token=SECRET");
			}

			@Override
			public CheckResult probeRole(String roleId, String channelId)
			{
				return new CheckResult(true, "x");
			}
		};
		server.stop();
		MutableClock clock = new MutableClock();
		server = new WebServer(0, settings, new CredentialStore(tmp), new SessionManager(clock),
				new LoginThrottle(clock), new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "x")),
				bot, boom);
		server.start();
		login("owner", "correct horse");
		Res r = call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", M1));
		assertEquals(200, r.status(), r.body());
		assertFalse(r.json().get("ok").getAsBoolean());
		assertFalse(r.body().contains("SECRET"));
	}

	@Test
	void absentKeyFallsBackToTheSavedValue() throws Exception
	{
		start(Map.of("TEXT_CHANNELID", C1, "MESSAGEID", M1));
		ready();
		call("POST", "/api/test/status", "{}");
		assertEquals(List.of("message|" + C1 + "|" + M1), probe.calls);
		call("POST", "/api/test/status", ids("statusMessageId", "  999999999999999999  "));
		assertEquals("message|" + C1 + "|999999999999999999", probe.calls.get(1));
	}

	@Test
	void presentBlankValueIsNotReplacedByTheSavedValue() throws Exception
	{
		start(Map.of("TEXT_CHANNELID", C1, "MESSAGEID", M1));
		ready();
		Res r = call("POST", "/api/test/status", ids("statusChannelId", "", "statusMessageId", ""));
		assertFalse(r.json().get("ok").getAsBoolean());
		assertEquals("Enter the channel ID and the message ID first", r.json().get("message").getAsString());
		assertTrue(probe.calls.isEmpty());
	}

	@Test
	void badIdGivesFixedNotOkAnswerWithoutCallingTheProbe() throws Exception
	{
		ready();
		Res r = call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", "12ab"));
		assertEquals(200, r.status());
		assertFalse(r.json().get("ok").getAsBoolean());
		assertEquals("Status message ID must be the number Discord shows, 17 to 20 digits",
				r.json().get("message").getAsString());
		assertTrue(probe.calls.isEmpty());
	}

	@Test
	void nothingToTestGivesFixedAnswerWithoutCallingTheProbe() throws Exception
	{
		ready();
		Res r = call("POST", "/api/test/queue", "{}");
		assertEquals(200, r.status());
		assertFalse(r.json().get("ok").getAsBoolean());
		assertTrue(probe.calls.isEmpty());
	}

	@Test
	void roleTestChecksMessageThenRole() throws Exception
	{
		ready();
		Res r = call("POST", "/api/test/role",
				ids("roleId", R1, "roleChannelId", C1, "roleMessageId", M1));
		assertEquals(200, r.status(), r.body());
		assertTrue(r.json().get("ok").getAsBoolean());
		assertEquals(List.of("message|" + C1 + "|" + M1, "role|" + R1 + "|" + C1), probe.calls);
	}

	@Test
	void roleTestStopsAtTheMessageWhenItFails() throws Exception
	{
		ready();
		probe.result = new CheckResult(false, "msg problem");
		Res r = call("POST", "/api/test/role", ids("roleId", R1, "roleChannelId", C1, "roleMessageId", M1));
		assertEquals("msg problem", r.json().get("message").getAsString());
		assertEquals(1, probe.calls.size());
	}

	@Test
	void roleProblemIsReported() throws Exception
	{
		ready();
		probe.roleResult = new CheckResult(false, "That role cannot be used.");
		Res r = call("POST", "/api/test/role", ids("roleId", R1, "roleChannelId", C1, "roleMessageId", M1));
		assertFalse(r.json().get("ok").getAsBoolean());
		assertEquals("That role cannot be used.", r.json().get("message").getAsString());
	}

	@Test
	void roleTestWithBadRoleIdDoesNotCallTheProbe() throws Exception
	{
		ready();
		Res r = call("POST", "/api/test/role", ids("roleId", "nope", "roleChannelId", C1, "roleMessageId", M1));
		assertFalse(r.json().get("ok").getAsBoolean());
		assertEquals("Role ID must be the number Discord shows, 17 to 20 digits", r.json().get("message").getAsString());
		assertTrue(probe.calls.isEmpty());
	}

	@Test
	void testRoutesNeedLoginCsrfAndNewCredentials() throws Exception
	{
		for (String section : new String[] {"status", "queue", "role"})
		{
			String path = "/api/test/" + section;
			assertEquals(401, call("POST", path, "{}", false, false).status());
			assertEquals(405, call("GET", path, null, false, false).status());
		}
		login("admin", "admin");
		assertEquals(403, call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", M1)).status());
		ready();
		assertEquals(403, call("POST", "/api/test/status", ids("statusChannelId", C1, "statusMessageId", M1),
				true, false).status());
		assertTrue(probe.calls.isEmpty());
	}

	@Test
	void putRejectsABadIdWithNothingSaved() throws Exception
	{
		ready();
		Res r = call("PUT", "/api/settings", ids("plexIp", "1.2.3.4", "queueMessageId", "123"));
		assertEquals(400, r.status());
		assertEquals("Queue message ID must be the number Discord shows, 17 to 20 digits",
				r.json().get("error").getAsString());
		assertEquals("", settings.fileValues().plexIp());
		assertEquals("", settings.fileValues().queueMessageId());
		assertTrue(bot.restarts.isEmpty());
	}

	@Test
	void putTrimsAndSavesIds() throws Exception
	{
		ready();
		Res r = call("PUT", "/api/settings", ids("roleId", " " + R1 + " ", "roleChannelId", C1, "roleMessageId", M1));
		assertEquals(200, r.status(), r.body());
		assertEquals(R1, settings.fileValues().roleId());
		assertEquals(C1, settings.fileValues().roleChannelId());
		Res g = call("GET", "/api/settings", null);
		assertEquals(M1, g.json().get("roleMessageId").getAsString());
	}

	@Test
	void putCanClearAnIdWithEmptyString() throws Exception
	{
		ready();
		call("PUT", "/api/settings", ids("statusChannelId", C1, "statusMessageId", M1));
		call("PUT", "/api/settings", ids("statusMessageId", ""));
		assertEquals("", settings.fileValues().statusMessageId());
		assertEquals(C1, settings.fileValues().statusChannelId());
	}

	@Test
	void stalePageNeverOverwritesIdsSavedAfterItLoaded() throws Exception
	{
		ready();
		// the page loaded, then /setupstatus saved new ids, then the user saved a change to another field only
		settings.update(s -> org.example.listeners.SetupCommandsListener.apply(s, "setupstatus", C1, M1, ""));
		Res r = call("PUT", "/api/settings", ids("plexIp", "1.2.3.4"));
		assertEquals(200, r.status(), r.body());
		assertEquals(C1, settings.fileValues().statusChannelId());
		assertEquals(M1, settings.fileValues().statusMessageId());
		assertEquals("1.2.3.4", settings.fileValues().plexIp());
	}
}
