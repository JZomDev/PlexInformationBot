package org.example.announce;

import com.sun.net.httpserver.HttpServer;
import org.example.config.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpArrSourceTest
{
	private static final String KEY = "SECRET123";

	private static final String MOVIES = """
			[{"tmdbId":11,"title":"Has File","year":1999,"overview":"Plot","genres":["Action","Drama"],
			"runtime":120,"hasFile":true,"added":"2024-05-01T10:00:00Z",
			"images":[{"coverType":"fanart","remoteUrl":"http://x/f.jpg"},{"coverType":"poster","remoteUrl":"http://x/p.jpg"}],
			"movieFile":{"quality":{"quality":{"name":"Bluray-1080p"}}}},
			{"tmdbId":12,"title":"No File","year":2000,"hasFile":false,"added":"2024-05-01T10:00:00Z"},
			{"tmdbId":13,"title":"No Poster","year":2001,"hasFile":true,"added":"2024-05-02T10:00:00Z","images":[]}]
			""";

	private static final String SERIES = """
			[{"tvdbId":77,"title":"Show","year":2020,"overview":"About","genres":["Comedy"],
			"added":"2024-06-01T00:00:00Z",
			"images":[{"coverType":"poster","remoteUrl":"http://x/s.jpg"}],
			"seasons":[
			{"seasonNumber":0,"statistics":{"episodeCount":2,"episodeFileCount":2}},
			{"seasonNumber":1,"statistics":{"episodeCount":8,"episodeFileCount":8,"totalEpisodeCount":8}},
			{"seasonNumber":2,"statistics":{"episodeCount":8,"episodeFileCount":8,"nextAiring":"2030-01-01T00:00:00Z"}},
			{"seasonNumber":3,"statistics":{"episodeCount":8,"episodeFileCount":5}},
			{"seasonNumber":4,"statistics":{"episodeCount":0,"episodeFileCount":0}}]}]
			""";

	private HttpServer fake;
	private final List<String> paths = new ArrayList<>();
	private final List<String> keys = new ArrayList<>();
	private int status = 200;
	private String movieReply = MOVIES;
	private String seriesReply = SERIES;
	private String hostPort;

	@BeforeEach
	void setUp() throws Exception
	{
		fake = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		fake.createContext("/", ex ->
		{
			synchronized (paths)
			{
				paths.add(ex.getRequestURI().getPath());
				keys.add(ex.getRequestHeaders().getFirst("X-Api-Key"));
			}
			String reply = ex.getRequestURI().getPath().endsWith("/movie") ? movieReply : seriesReply;
			byte[] b = reply.getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(status, b.length);
			ex.getResponseBody().write(b);
			ex.close();
		});
		fake.start();
		hostPort = "127.0.0.1:" + fake.getAddress().getPort();
	}

	@AfterEach
	void tearDown()
	{
		fake.stop(0);
	}

	private HttpArrSource source(String radarrUrl, String radarrKey, String sonarrUrl, String sonarrKey)
	{
		Settings s = new Settings("", "", "", "", "", "", radarrUrl, radarrKey, sonarrUrl, sonarrKey, "", "", "", "",
				"", "", "", "", "", "");
		return new HttpArrSource(() -> s, HttpClient.newHttpClient());
	}

	private HttpArrSource source()
	{
		return source(hostPort, KEY, hostPort, KEY);
	}

	@Test
	void moviesReturnsOnlyOnesWithFile() throws Exception
	{
		List<Item> items = source().movies();
		assertEquals(2, items.size());
		Item m = items.get(0);
		assertEquals("movie", m.type());
		assertEquals("11", m.key());
		assertEquals("Has File", m.title());
		assertEquals(1999, m.year());
		assertEquals("Plot", m.summary());
		assertEquals("Action, Drama · 120m · Bluray-1080p", m.details());
		assertEquals("http://x/p.jpg", m.posterUrl());
		assertEquals(Instant.parse("2024-05-01T10:00:00Z"), m.added());
		assertEquals("/api/v3/movie", paths.get(0));
		assertEquals(KEY, keys.get(0));
	}

	@Test
	void missingPosterAndPartsAreBlankOrOmitted() throws Exception
	{
		Item m = source().movies().get(1);
		assertEquals("13", m.key());
		assertEquals("", m.posterUrl());
		assertEquals("", m.details());
		assertEquals("", m.summary());
	}

	@Test
	void seasonsReturnsOnlyCompleteNonSpecialSeasons() throws Exception
	{
		List<Item> items = source().seasons();
		assertEquals(1, items.size());
		Item s = items.get(0);
		assertEquals("season", s.type());
		assertEquals("77:1", s.key());
		assertEquals("Show · Season 1", s.title());
		assertEquals(2020, s.year());
		assertEquals("Comedy · 8 episodes", s.details());
		assertEquals("http://x/s.jpg", s.posterUrl());
		assertEquals(Instant.parse("2024-06-01T00:00:00Z"), s.added());
		assertEquals("/api/v3/series", paths.get(0));
	}

	@Test
	void unauthorizedThrowsWithoutKeyOrUrl()
	{
		status = 401;
		IOException e = assertThrows(IOException.class, () -> source().movies());
		assertFalse(e.getMessage().contains(KEY));
		assertFalse(e.getMessage().contains("127.0.0.1"));
		e = assertThrows(IOException.class, () -> source().seasons());
		assertFalse(e.getMessage().contains(KEY));
		assertFalse(e.getMessage().contains("127.0.0.1"));
	}

	@Test
	void badJsonThrowsWithoutKeyOrUrl()
	{
		movieReply = "not json";
		IOException e = assertThrows(IOException.class, () -> source().movies());
		assertFalse(e.getMessage().contains(KEY));
		assertFalse(e.getMessage().contains("127.0.0.1"));
	}

	@Test
	void connectFailureThrowsWithoutKeyOrUrl()
	{
		fake.stop(0);
		IOException e = assertThrows(IOException.class, () -> source().movies());
		assertFalse(e.getMessage().contains(KEY));
		assertFalse(e.getMessage().contains("127.0.0.1"));
	}

	@Test
	void blankSettingsGiveEmptyListsWithoutRequests() throws Exception
	{
		assertTrue(source("", KEY, "", KEY).movies().isEmpty());
		assertTrue(source(hostPort, "", hostPort, "").movies().isEmpty());
		assertTrue(source("", KEY, "", KEY).seasons().isEmpty());
		assertTrue(source(hostPort, "", hostPort, "").seasons().isEmpty());
		assertTrue(paths.isEmpty());
	}
}
