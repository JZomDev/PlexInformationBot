package org.example.web;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Connection tests for the setup page. Messages are fixed texts and never contain a token or key. */
public class ConnectionChecks
{
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final HttpClient http;
	private final DiscordProbe discord;

	public ConnectionChecks(HttpClient http, DiscordProbe discord)
	{
		this.http = http;
		this.discord = discord;
	}

	public CheckResult discord(String token)
	{
		if (isBlank(token))
		{
			return fail("Bot token is required");
		}
		CheckResult r;
		try
		{
			r = discord.probe(token);
		}
		catch (RuntimeException e)
		{
			return fail("Could not log in");
		}
		return new CheckResult(r.ok(), mask(r.message(), token));
	}

	public CheckResult plex(String ip, String port, String token)
	{
		String host = cleanHost(ip);
		if (host == null || isBlank(port) || isBlank(token))
		{
			return fail("IP, port and token are required");
		}
		String p = port.trim();
		if (!p.matches("\\d{1,5}"))
		{
			return fail("Invalid port");
		}
		HttpRequest.Builder b = request("http://" + host + ":" + p + "/");
		if (b == null)
		{
			return fail("Invalid address");
		}
		b.header("Accept", "application/json").header("X-Plex-Token", token);
		return run(b.build(), "Rejected: check the token", token, body ->
		{
			String name = null;
			try
			{
				JsonObject o = HttpIo.GSON.fromJson(body, JsonObject.class);
				name = o.getAsJsonObject("MediaContainer").get("friendlyName").getAsString();
			}
			catch (RuntimeException ignored)
			{
				// fall back to a generic message
			}
			return name == null ? new CheckResult(true, "Connected to Plex")
					: new CheckResult(true, "Connected to Plex server " + name);
		});
	}

	public CheckResult tautulli(String url, String key)
	{
		String host = cleanHost(url);
		if (host == null || isBlank(key))
		{
			return fail("URL and API key are required");
		}
		HttpRequest.Builder b = request("http://" + host + "/api/v2?apikey="
				+ URLEncoder.encode(key, StandardCharsets.UTF_8) + "&cmd=get_server_info");
		if (b == null)
		{
			return fail("Invalid address");
		}
		return run(b.build(), "Rejected: check the key", key, body ->
		{
			try
			{
				JsonObject o = HttpIo.GSON.fromJson(body, JsonObject.class);
				if ("success".equals(o.getAsJsonObject("response").get("result").getAsString()))
				{
					return new CheckResult(true, "Connected to Tautulli");
				}
			}
			catch (RuntimeException ignored)
			{
				// treated as a rejection below
			}
			return fail("Rejected: check the key");
		});
	}

	public CheckResult radarr(String url, String key)
	{
		return arr("Radarr", url, key);
	}

	public CheckResult sonarr(String url, String key)
	{
		return arr("Sonarr", url, key);
	}

	private CheckResult arr(String name, String url, String key)
	{
		String host = cleanHost(url);
		if (host == null || isBlank(key))
		{
			return fail("URL and API key are required");
		}
		HttpRequest.Builder b = request("http://" + host + "/api/v3/system/status");
		if (b == null)
		{
			return fail("Invalid address");
		}
		b.header("X-Api-Key", key);
		return run(b.build(), "Rejected: check the key", key, body -> new CheckResult(true, "Connected to " + name));
	}

	private interface OnOk
	{
		CheckResult apply(String body);
	}

	private CheckResult run(HttpRequest req, String rejected, String secret, OnOk onOk)
	{
		try
		{
			HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
			int code = res.statusCode();
			if (code == 401 || code == 403)
			{
				return fail(rejected);
			}
			if (code < 200 || code > 299)
			{
				return fail("Unexpected response (HTTP " + code + ")");
			}
			CheckResult r = onOk.apply(res.body());
			return new CheckResult(r.ok(), mask(r.message(), secret));
		}
		catch (HttpTimeoutException e)
		{
			return fail("Timed out");
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return fail("Interrupted");
		}
		catch (IOException | RuntimeException e)
		{
			// never use the exception text: it embeds the URL
			return fail("Could not connect");
		}
	}

	private static HttpRequest.Builder request(String url)
	{
		try
		{
			return HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET();
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	/** Strips a pasted scheme and trailing slashes; null when empty or unsafe to put in a URL. */
	static String cleanHost(String value)
	{
		if (value == null)
		{
			return null;
		}
		String v = value.trim().replaceFirst("(?i)^https?://", "");
		while (v.endsWith("/"))
		{
			v = v.substring(0, v.length() - 1);
		}
		if (v.isEmpty() || v.matches(".*[\\s/?#\\\\@].*"))
		{
			return null;
		}
		return v;
	}

	private static String mask(String message, String secret)
	{
		return isBlank(secret) ? message : message.replace(secret, "********");
	}

	private static boolean isBlank(String s)
	{
		return s == null || s.isBlank();
	}

	private static CheckResult fail(String message)
	{
		return new CheckResult(false, message);
	}
}
