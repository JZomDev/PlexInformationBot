package org.example.web;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionChecksTest
{
	private static final String SECRET = "SECRET123";

	private record Seen(String uri, String accept, String plexToken, String apiKey)
	{
	}

	private HttpServer fake;
	private final List<Seen> seen = new ArrayList<>();
	private int status = 200;
	private String reply = "{}";
	private ConnectionChecks checks;
	private String hostPort;

	@BeforeEach
	void setUp() throws Exception
	{
		fake = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		fake.createContext("/", ex ->
		{
			synchronized (seen)
			{
				seen.add(new Seen(ex.getRequestURI().toString(), ex.getRequestHeaders().getFirst("Accept"),
						ex.getRequestHeaders().getFirst("X-Plex-Token"), ex.getRequestHeaders().getFirst("X-Api-Key")));
			}
			byte[] b = reply.getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(status, b.length);
			ex.getResponseBody().write(b);
			ex.close();
		});
		fake.start();
		hostPort = "127.0.0.1:" + fake.getAddress().getPort();
		checks = new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(true, "unused"));
	}

	@AfterEach
	void tearDown()
	{
		fake.stop(0);
	}

	@Test
	void plexOkReportsFriendlyName()
	{
		reply = "{\"MediaContainer\":{\"friendlyName\":\"Living Room\"}}";
		CheckResult r = checks.plex("127.0.0.1", String.valueOf(fake.getAddress().getPort()), SECRET);
		assertTrue(r.ok(), r.message());
		assertTrue(r.message().contains("Living Room"));
		assertEquals(1, seen.size());
		assertEquals("/", seen.get(0).uri());
		assertEquals("application/json", seen.get(0).accept());
		assertEquals(SECRET, seen.get(0).plexToken());
	}

	@Test
	void arrBadKeyReports401AsNotOk()
	{
		status = 401;
		CheckResult r = checks.radarr(hostPort, SECRET);
		assertFalse(r.ok());
		assertEquals("/api/v3/system/status", seen.get(0).uri());
		assertEquals(SECRET, seen.get(0).apiKey());
		assertFalse(r.message().contains(SECRET));
	}

	@Test
	void arrOkAndSchemeAndSlashAreTolerated()
	{
		assertTrue(checks.sonarr("http://" + hostPort + "/", SECRET).ok());
		assertEquals("/api/v3/system/status", seen.get(0).uri());
	}

	@Test
	void tautulliChecksResultField()
	{
		reply = "{\"response\":{\"result\":\"success\"}}";
		assertTrue(checks.tautulli(hostPort, SECRET).ok());
		assertEquals("/api/v2?apikey=" + SECRET + "&cmd=get_server_info", seen.get(0).uri());
		reply = "{\"response\":{\"result\":\"error\"}}";
		assertFalse(checks.tautulli(hostPort, SECRET).ok());
	}

	@Test
	void unreachableHostIsNotOkAndTimesOutUnder6s()
	{
		long start = System.nanoTime();
		CheckResult r = checks.radarr("192.0.2.1:7878", SECRET);
		long ms = (System.nanoTime() - start) / 1_000_000;
		assertFalse(r.ok());
		assertTrue(ms < 6000, "took " + ms);
	}

	@Test
	void messagesNeverContainTokenOrKey() throws Exception
	{
		int port = fake.getAddress().getPort();
		fake.stop(0);
		List<CheckResult> all = new ArrayList<>();
		all.add(checks.plex("127.0.0.1", String.valueOf(port), SECRET));
		all.add(checks.radarr("127.0.0.1:" + port, SECRET));
		all.add(checks.sonarr("127.0.0.1:" + port, SECRET));
		all.add(checks.tautulli("127.0.0.1:" + port, SECRET));
		all.add(new ConnectionChecks(HttpClient.newHttpClient(), t -> new CheckResult(false, "bad " + t))
				.discord(SECRET));
		all.add(checks.tautulli("bad host" + SECRET, SECRET));
		for (CheckResult r : all)
		{
			assertFalse(r.ok());
			assertFalse(r.message().contains(SECRET), r.message());
		}
	}

	@Test
	void emptyRequiredFieldIsNotOkWithoutNetworkCall()
	{
		assertFalse(checks.plex("", "32400", SECRET).ok());
		assertFalse(checks.plex("127.0.0.1", "", SECRET).ok());
		assertFalse(checks.plex("127.0.0.1", "32400", "").ok());
		assertFalse(checks.radarr("", SECRET).ok());
		assertFalse(checks.sonarr(hostPort, "").ok());
		assertFalse(checks.tautulli(hostPort, null).ok());
		assertFalse(checks.discord("").ok());
		assertTrue(seen.isEmpty());
	}

	@Test
	void hostWithUnsafeCharactersIsRejectedWithoutNetworkCall()
	{
		assertFalse(checks.radarr(hostPort + "/evil?x=1", SECRET).ok());
		assertFalse(checks.radarr("a b:1", SECRET).ok());
		assertFalse(checks.radarr(hostPort + "#frag", SECRET).ok());
		assertTrue(seen.isEmpty());
	}

	@Test
	void discordUsesProbe()
	{
		AtomicInteger calls = new AtomicInteger();
		ConnectionChecks c = new ConnectionChecks(HttpClient.newHttpClient(), t ->
		{
			calls.incrementAndGet();
			assertEquals(SECRET, t);
			return new CheckResult(true, "Logged in as Bot");
		});
		CheckResult r = c.discord(SECRET);
		assertTrue(r.ok());
		assertEquals("Logged in as Bot", r.message());
		assertEquals(1, calls.get());
	}

	@Test
	void plexMessageMasksSecretEchoedByRemote()
	{
		reply = "{\"MediaContainer\":{\"friendlyName\":\"srv-" + SECRET + "\"}}";
		CheckResult r = checks.plex("127.0.0.1", String.valueOf(fake.getAddress().getPort()), SECRET);
		assertTrue(r.ok(), r.message());
		assertTrue(r.message().contains("srv-"));
		assertFalse(r.message().contains(SECRET), r.message());
	}

	@Test
	void arrAndTautulliMessagesIgnoreRemoteBodyEchoingKey()
	{
		reply = "{\"error\":\"bad key " + SECRET + "\",\"response\":{\"result\":\"error\",\"message\":\"" + SECRET + "\"}}";
		status = 500;
		assertFalse(checks.radarr(hostPort, SECRET).message().contains(SECRET));
		assertFalse(checks.sonarr(hostPort, SECRET).message().contains(SECRET));
		status = 200;
		CheckResult t = checks.tautulli(hostPort, SECRET);
		assertFalse(t.ok());
		assertFalse(t.message().contains(SECRET));
	}
}
