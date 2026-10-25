package org.example.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.bot.BotController;
import org.example.config.Settings;
import org.example.config.SettingsStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer
{
	private static final String COOKIE = "sid";
	private static final String TEST_PREFIX = "/api/test/";
	private static final List<String> TEST_SECTIONS = List.of("discord", "plex", "tautulli", "radarr", "sonarr");

	private interface Route
	{
		void handle(HttpExchange ex, SessionManager.Session session) throws Exception;
	}

	private record Api(String method, boolean needsSession, boolean exemptFromMustChange, Route route)
	{
	}

	private final int requestedPort;
	private final SettingsStore settings;
	private final CredentialStore credentials;
	private final SessionManager sessions;
	private final LoginThrottle throttle;
	private final ConnectionChecks checks;
	private final BotController bot;
	private final Map<String, Api> routes = new LinkedHashMap<>();
	private final Object saveLock = new Object();
	private HttpServer server;
	private ExecutorService executor;

	public WebServer(int port, SettingsStore settings, CredentialStore credentials, SessionManager sessions,
					 LoginThrottle throttle, ConnectionChecks checks, BotController bot)
	{
		this.requestedPort = port;
		this.settings = settings;
		this.credentials = credentials;
		this.sessions = sessions;
		this.throttle = throttle;
		this.checks = checks;
		this.bot = bot;
		routes.put("/api/session", new Api("GET", false, true, this::session));
		routes.put("/api/login", new Api("POST", false, true, (ex, s) -> login(ex)));
		routes.put("/api/logout", new Api("POST", false, true, this::logout));
		routes.put("/api/credentials", new Api("POST", true, true, this::changeCredentials));
		routes.put("/api/status", new Api("GET", true, false, this::status));
		routes.put("/api/settings", new Api("GET", true, false, this::getSettings));
	}

	public void start() throws IOException
	{
		server = HttpServer.create(new InetSocketAddress(requestedPort), 0);
		executor = Executors.newFixedThreadPool(4);
		server.setExecutor(executor);
		server.createContext("/", this::dispatch);
		server.start();
	}

	public void stop()
	{
		if (server != null)
		{
			server.stop(0);
		}
		if (executor != null)
		{
			executor.shutdownNow();
		}
	}

	public int port()
	{
		return server.getAddress().getPort();
	}

	private void dispatch(HttpExchange ex) throws IOException
	{
		try
		{
			ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
			ex.getResponseHeaders().set("X-Frame-Options", "DENY");
			String path = ex.getRequestURI().getPath();
			if (path != null && path.startsWith("/api/"))
			{
				ex.getResponseHeaders().set("Cache-Control", "no-store");
				api(ex, path);
			}
			else
			{
				StaticFiles.serve(ex);
			}
		}
		catch (HttpIo.TooLarge e)
		{
			HttpIo.sendError(ex, 413, "Request body too large");
		}
		catch (Exception e)
		{
			// never echo the exception: it may carry paths or submitted values
			try
			{
				HttpIo.sendError(ex, 500, "Internal error");
			}
			catch (Exception ignored)
			{
				ex.close();
			}
		}
	}

	private void api(HttpExchange ex, String path) throws Exception
	{
		Api api = path.equals("/api/settings") && "PUT".equals(ex.getRequestMethod())
				? new Api("PUT", true, false, this::putSettings) : routeFor(path);
		if (api == null)
		{
			HttpIo.sendError(ex, 404, "Not found");
			return;
		}
		if (!api.method().equals(ex.getRequestMethod()))
		{
			ex.getResponseHeaders().set("Allow", allowed(path));
			HttpIo.sendError(ex, 405, "Method not allowed");
			return;
		}
		SessionManager.Session session = currentSession(ex).orElse(null);
		if (api.needsSession() && session == null)
		{
			HttpIo.sendError(ex, 401, "Not logged in");
			return;
		}
		if (!api.method().equals("GET") && !path.equals("/api/login") && session != null
				&& !csrfOk(ex, session))
		{
			HttpIo.sendError(ex, 403, "Invalid CSRF token");
			return;
		}
		if (!api.exemptFromMustChange() && credentials.mustChange())
		{
			HttpIo.sendError(ex, 403, "You must change the default credentials first");
			return;
		}
		api.route().handle(ex, session);
	}

	private Api routeFor(String path)
	{
		if (path.startsWith(TEST_PREFIX))
		{
			String section = path.substring(TEST_PREFIX.length());
			return TEST_SECTIONS.contains(section)
					? new Api("POST", true, false, (ex, s) -> test(ex, section)) : null;
		}
		return routes.get(path);
	}

	private String allowed(String path)
	{
		if (path.startsWith(TEST_PREFIX))
		{
			return "POST";
		}
		return path.equals("/api/settings") ? "GET, PUT" : routes.get(path).method();
	}

	private Optional<SessionManager.Session> currentSession(HttpExchange ex)
	{
		List<String> cookies = ex.getRequestHeaders().get("Cookie");
		if (cookies == null)
		{
			return Optional.empty();
		}
		for (String header : cookies)
		{
			for (String part : header.split(";"))
			{
				String p = part.trim();
				if (p.startsWith(COOKIE + "="))
				{
					return sessions.get(p.substring(COOKIE.length() + 1));
				}
			}
		}
		return Optional.empty();
	}

	private static boolean csrfOk(HttpExchange ex, SessionManager.Session session)
	{
		String sent = ex.getRequestHeaders().getFirst("X-CSRF-Token");
		return sent != null && MessageDigest.isEqual(sent.getBytes(StandardCharsets.UTF_8),
				session.csrfToken().getBytes(StandardCharsets.UTF_8));
	}

	private static JsonObject parseObject(HttpExchange ex) throws IOException, HttpIo.TooLarge
	{
		try
		{
			JsonObject o = HttpIo.GSON.fromJson(HttpIo.readBody(ex), JsonObject.class);
			return o;
		}
		catch (JsonParseException | IllegalStateException e)
		{
			return null;
		}
	}

	private static String str(JsonObject o, String key)
	{
		return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
	}

	private static boolean sameOrigin(HttpExchange ex)
	{
		String origin = ex.getRequestHeaders().getFirst("Origin");
		if (origin == null)
		{
			return true;
		}
		String host = ex.getRequestHeaders().getFirst("Host");
		try
		{
			String authority = URI.create(origin).getRawAuthority();
			return host != null && authority != null && authority.equalsIgnoreCase(host);
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	private static String cookie(String value, int maxAge, boolean secure)
	{
		return COOKIE + "=" + value + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + maxAge
				+ (secure ? "; Secure" : "");
	}

	private static boolean https(HttpExchange ex)
	{
		return "https".equalsIgnoreCase(ex.getRequestHeaders().getFirst("X-Forwarded-Proto"));
	}

	private void session(HttpExchange ex, SessionManager.Session s) throws IOException
	{
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("loggedIn", s != null);
		body.put("mustChange", credentials.mustChange());
		body.put("csrf", s == null ? "" : s.csrfToken());
		HttpIo.sendJson(ex, 200, body);
	}

	private void login(HttpExchange ex) throws Exception
	{
		if (!sameOrigin(ex))
		{
			HttpIo.sendError(ex, 403, "Cross-origin request refused");
			return;
		}
		String key = ex.getRemoteAddress().getAddress().getHostAddress();
		if (!throttle.allowed(key))
		{
			HttpIo.sendError(ex, 429, "Too many attempts, try again later");
			return;
		}
		JsonObject in = parseObject(ex);
		String user = in == null ? null : str(in, "username");
		String pass = in == null ? null : str(in, "password");
		if (user == null || pass == null)
		{
			HttpIo.sendError(ex, 400, "Invalid request");
			return;
		}
		if (!credentials.verify(user, pass))
		{
			throttle.failure(key);
			HttpIo.sendError(ex, 401, "Invalid username or password");
			return;
		}
		throttle.success(key);
		currentSession(ex).ifPresent(old -> sessions.destroy(old.id()));
		SessionManager.Session created = sessions.create();
		ex.getResponseHeaders().add("Set-Cookie", cookie(created.id(), 12 * 3600, https(ex)));
		HttpIo.sendJson(ex, 200, Map.of("mustChange", credentials.mustChange(), "csrf", created.csrfToken()));
	}

	private void logout(HttpExchange ex, SessionManager.Session s) throws IOException
	{
		if (s != null)
		{
			sessions.destroy(s.id());
		}
		ex.getResponseHeaders().add("Set-Cookie", cookie("", 0, https(ex)));
		HttpIo.sendJson(ex, 200, Map.of("ok", true));
	}

	private void changeCredentials(HttpExchange ex, SessionManager.Session s) throws Exception
	{
		JsonObject in = parseObject(ex);
		String user = in == null ? null : str(in, "username");
		String pass = in == null ? null : str(in, "password");
		if (user == null || pass == null)
		{
			HttpIo.sendError(ex, 400, "Invalid request");
			return;
		}
		try
		{
			credentials.change(user, pass);
		}
		catch (IllegalArgumentException e)
		{
			// these messages are fixed texts from CredentialStore, never the submitted values
			HttpIo.sendError(ex, 400, e.getMessage());
			return;
		}
		catch (IOException e)
		{
			HttpIo.sendError(ex, 500, "Could not save credentials");
			return;
		}
		sessions.destroyAll();
		SessionManager.Session fresh = sessions.create();
		ex.getResponseHeaders().add("Set-Cookie", cookie(fresh.id(), 12 * 3600, https(ex)));
		HttpIo.sendJson(ex, 200, Map.of("ok", true, "csrf", fresh.csrfToken()));
	}

	private void getSettings(HttpExchange ex, SessionManager.Session s) throws IOException
	{
		JsonObject body = HttpIo.GSON.toJsonTree(settings.effective().masked()).getAsJsonObject();
		body.addProperty("warning", settings.loadWarning().orElse(null));
		HttpIo.sendJson(ex, 200, body);
	}

	/** Keys absent from the body keep the saved value; present keys (even empty) replace it. */
	private Settings withAbsentKept(JsonObject body, Settings saved)
	{
		JsonObject merged = HttpIo.GSON.toJsonTree(saved).getAsJsonObject();
		for (Map.Entry<String, com.google.gson.JsonElement> entry : body.entrySet())
		{
			if (merged.has(entry.getKey()) && !entry.getValue().isJsonNull())
			{
				merged.add(entry.getKey(), entry.getValue());
			}
		}
		return HttpIo.GSON.fromJson(merged, Settings.class);
	}

	private void putSettings(HttpExchange ex, SessionManager.Session s) throws Exception
	{
		JsonObject submitted;
		try
		{
			JsonObject body = HttpIo.GSON.fromJson(HttpIo.readBody(ex), JsonObject.class);
			if (body != null)
			{
				withAbsentKept(body, settings.fileValues());
			}
			submitted = body;
		}
		catch (JsonParseException | IllegalStateException e)
		{
			submitted = null;
		}
		if (submitted == null)
		{
			HttpIo.sendError(ex, 400, "Invalid request");
			return;
		}
		JsonObject form = submitted;
		synchronized (saveLock)
		{
			try
			{
				settings.update(saved -> saved.applyForm(withAbsentKept(form, saved)).normalisedHosts());
			}
			catch (IOException e)
			{
				HttpIo.sendError(ex, 500, "Could not save settings");
				return;
			}
			try
			{
				bot.restart(settings.effective());
			}
			catch (RuntimeException e)
			{
				HttpIo.sendError(ex, 500, "Settings saved, but the bot could not be restarted");
				return;
			}
		}
		HttpIo.sendJson(ex, 200, Map.of("ok", true));
	}

	private static final CheckResult READDRESS_RESULT = new CheckResult(false,
			"Re-enter the key/token to test a different address");

	/** A submitted field that is absent is resolved from the saved effective settings. */
	private static String field(JsonObject in, String key, String saved)
	{
		String v = str(in, key);
		return v == null ? saved : v;
	}

	/** True when the secret has to come from saved settings (absent or the mask). */
	private static boolean needsSaved(JsonObject in, String key)
	{
		String v = str(in, key);
		return v == null || Settings.MASK.equals(v);
	}

	private static boolean sameHost(String submitted, String saved)
	{
		String a = ConnectionChecks.cleanHost(submitted);
		String b = ConnectionChecks.cleanHost(saved);
		return a == null ? b == null : b != null && a.equalsIgnoreCase(b);
	}

	private static boolean samePort(String submitted, String saved)
	{
		return submitted.trim().equals(saved.trim());
	}

	/** The secret to test with, or null when a saved secret must not be sent to a changed address. */
	private static String secret(JsonObject in, String key, String saved, boolean sameAddress)
	{
		if (!needsSaved(in, key))
		{
			return str(in, key);
		}
		return sameAddress ? saved : null;
	}

	private void test(HttpExchange ex, String section) throws Exception
	{
		JsonObject in = parseObject(ex);
		if (in == null)
		{
			HttpIo.sendError(ex, 400, "Invalid request");
			return;
		}
		Settings e = settings.effective();
		CheckResult result = switch (section)
		{
			case "discord" -> checks.discord(secret(in, "botToken", e.botToken(), true));
			case "plex" ->
			{
				String ip = field(in, "plexIp", e.plexIp());
				String port = field(in, "plexPort", e.plexPort());
				boolean same = sameHost(ip, e.plexIp()) && samePort(port, e.plexPort());
				String token = secret(in, "plexToken", e.plexToken(), same);
				yield token == null ? READDRESS_RESULT : checks.plex(ip, port, token);
			}
			case "tautulli" ->
			{
				String url = field(in, "tautulliUrl", e.tautulliUrl());
				String key = secret(in, "tautulliApiKey", e.tautulliApiKey(), sameHost(url, e.tautulliUrl()));
				yield key == null ? READDRESS_RESULT : checks.tautulli(url, key);
			}
			case "radarr" ->
			{
				String url = field(in, "radarrUrl", e.radarrUrl());
				String key = secret(in, "radarrApiKey", e.radarrApiKey(), sameHost(url, e.radarrUrl()));
				yield key == null ? READDRESS_RESULT : checks.radarr(url, key);
			}
			default ->
			{
				String url = field(in, "sonarrUrl", e.sonarrUrl());
				String key = secret(in, "sonarrApiKey", e.sonarrApiKey(), sameHost(url, e.sonarrUrl()));
				yield key == null ? READDRESS_RESULT : checks.sonarr(url, key);
			}
		};
		HttpIo.sendJson(ex, 200, Map.of("ok", result.ok(), "message", result.message()));
	}

	private void status(HttpExchange ex, SessionManager.Session s) throws IOException
	{
		Settings e = settings.effective();
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("botState", bot.state().name());
		body.put("queueSet", !e.queueChannelId().isEmpty() && !e.queueMessageId().isEmpty());
		body.put("statusSet", !e.statusChannelId().isEmpty() && !e.statusMessageId().isEmpty());
		body.put("roleSet", !e.roleChannelId().isEmpty() && !e.roleMessageId().isEmpty());
		HttpIo.sendJson(ex, 200, body);
	}
}
