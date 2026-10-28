package org.example.announce;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.config.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Reads qualifying movies from Radarr and finished seasons from Sonarr. Messages never hold the key or URL. */
public class HttpArrSource implements ArrSource
{
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
	private static final Gson GSON = new Gson();

	private final Supplier<Settings> settings;
	private final HttpClient client;

	public HttpArrSource(Supplier<Settings> settings, HttpClient client)
	{
		this.settings = settings;
		this.client = client;
	}

	@Override
	public boolean configured(String type)
	{
		Settings s = settings.get();
		return Announcer.MOVIE.equals(type) ? usable(s.radarrUrl(), s.radarrApiKey())
				: usable(s.sonarrUrl(), s.sonarrApiKey());
	}

	private static boolean usable(String url, String key)
	{
		return !host(url).isEmpty() && key != null && !key.isBlank();
	}

	private static String host(String url)
	{
		return url == null ? "" : url.trim().replaceFirst("(?i)^https?://", "").replaceAll("/+$", "");
	}

	@Override
	public List<Item> movies() throws IOException
	{
		Settings s = settings.get();
		JsonArray movies = fetch(s.radarrUrl(), s.radarrApiKey(), "movie");
		List<Item> out = new ArrayList<>();
		for (JsonElement e : movies)
		{
			try
			{
				JsonObject m = e.getAsJsonObject();
				if (!bool(m, "hasFile") || !m.has("tmdbId") || m.get("tmdbId").isJsonNull())
				{
					continue;
				}
				List<String> parts = new ArrayList<>();
				String genres = genres(m);
				if (!genres.isEmpty())
				{
					parts.add(genres);
				}
				if (m.has("runtime") && !m.get("runtime").isJsonNull() && m.get("runtime").getAsInt() > 0)
				{
					parts.add(m.get("runtime").getAsInt() + "m");
				}
				String quality = quality(m);
				if (!quality.isEmpty())
				{
					parts.add(quality);
				}
				out.add(new Item(Announcer.MOVIE, m.get("tmdbId").getAsString(), str(m, "title"), integer(m, "year"),
						str(m, "overview"), String.join(" · ", parts), poster(m), added(m)));
			}
			catch (RuntimeException ex)
			{
				throw new IOException("Unexpected response from Radarr");
			}
		}
		return out;
	}

	@Override
	public List<Item> seasons() throws IOException
	{
		Settings s = settings.get();
		JsonArray series = fetch(s.sonarrUrl(), s.sonarrApiKey(), "series");
		List<Item> out = new ArrayList<>();
		for (JsonElement e : series)
		{
			try
			{
				JsonObject sr = e.getAsJsonObject();
				if (!sr.has("tvdbId") || sr.get("tvdbId").isJsonNull() || !sr.has("seasons"))
				{
					continue;
				}
				String tvdb = sr.get("tvdbId").getAsString();
				String genres = genres(sr);
				for (JsonElement se : sr.getAsJsonArray("seasons"))
				{
					JsonObject season = se.getAsJsonObject();
					int number = integer(season, "seasonNumber");
					if (number <= 0 || !season.has("statistics") || !season.get("statistics").isJsonObject())
					{
						continue;
					}
					JsonObject st = season.getAsJsonObject("statistics");
					int aired = integer(st, "episodeCount");
					int files = integer(st, "episodeFileCount");
					boolean upcoming = st.has("nextAiring") && !st.get("nextAiring").isJsonNull();
					int total = integer(st, "totalEpisodeCount");
					if (aired <= 0 || files < aired || files < total || upcoming)
					{
						continue;
					}
					String details = (genres.isEmpty() ? "" : genres + " · ") + files + " episodes";
					String seriesTitle = str(sr, "title");
					String title = seriesTitle.isBlank() ? "" : seriesTitle + " · Season " + number;
					out.add(new Item(Announcer.SEASON, tvdb + ":" + number, title,
							integer(sr, "year"), str(sr, "overview"), details, poster(sr), added(sr)));
				}
			}
			catch (RuntimeException ex)
			{
				throw new IOException("Unexpected response from Sonarr");
			}
		}
		return out;
	}

	private JsonArray fetch(String url, String key, String path) throws IOException
	{
		String name = "movie".equals(path) ? "Radarr" : "Sonarr";
		String host = host(url);
		if (!usable(url, key))
		{
			throw new IOException(name + " is not configured");
		}
		HttpRequest req;
		try
		{
			req = HttpRequest.newBuilder(URI.create("http://" + host + "/api/v3/" + path)).timeout(REQUEST_TIMEOUT)
					.header("X-Api-Key", key).header("Accept", "application/json").GET().build();
		}
		catch (IllegalArgumentException e)
		{
			throw new IOException("Invalid " + name + " address");
		}
		HttpResponse<String> res;
		try
		{
			res = client.send(req, HttpResponse.BodyHandlers.ofString());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new IOException(name + " request interrupted");
		}
		catch (IOException e)
		{
			// never pass the cause on: its text embeds the URL
			throw new IOException("Could not reach " + name);
		}
		int code = res.statusCode();
		if (code < 200 || code > 299)
		{
			throw new IOException(name + " returned HTTP " + code);
		}
		try
		{
			JsonArray arr = GSON.fromJson(res.body(), JsonArray.class);
			if (arr == null)
			{
				throw new IOException("Unexpected response from " + name);
			}
			return arr;
		}
		catch (RuntimeException e)
		{
			throw new IOException("Unexpected response from " + name);
		}
	}

	private static String poster(JsonObject o)
	{
		if (o.has("images") && o.get("images").isJsonArray())
		{
			for (JsonElement e : o.getAsJsonArray("images"))
			{
				JsonObject img = e.getAsJsonObject();
				if ("poster".equals(str(img, "coverType")))
				{
					return str(img, "remoteUrl");
				}
			}
		}
		return "";
	}

	private static String quality(JsonObject m)
	{
		try
		{
			return str(m.getAsJsonObject("movieFile").getAsJsonObject("quality").getAsJsonObject("quality"), "name");
		}
		catch (RuntimeException e)
		{
			return "";
		}
	}

	private static String genres(JsonObject o)
	{
		List<String> g = new ArrayList<>();
		if (o.has("genres") && o.get("genres").isJsonArray())
		{
			for (JsonElement e : o.getAsJsonArray("genres"))
			{
				if (!e.isJsonNull() && !e.getAsString().isBlank())
				{
					g.add(e.getAsString());
				}
			}
		}
		return String.join(", ", g);
	}

	private static Instant added(JsonObject o)
	{
		String v = str(o, "added");
		try
		{
			return v.isEmpty() ? Instant.now() : Instant.parse(v);
		}
		catch (RuntimeException e)
		{
			return Instant.now();
		}
	}

	private static boolean bool(JsonObject o, String name)
	{
		return o.has(name) && !o.get(name).isJsonNull() && o.get(name).getAsBoolean();
	}

	private static int integer(JsonObject o, String name)
	{
		return o.has(name) && !o.get(name).isJsonNull() ? o.get(name).getAsInt() : 0;
	}

	private static String str(JsonObject o, String name)
	{
		return o != null && o.has(name) && !o.get(name).isJsonNull() ? o.get(name).getAsString() : "";
	}
}
