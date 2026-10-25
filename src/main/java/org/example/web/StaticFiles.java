package org.example.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

/** Serves a fixed set of file types from the classpath folder /web/. */
final class StaticFiles
{
	private static final Pattern SAFE_PATH = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*(/[A-Za-z0-9_-][A-Za-z0-9._-]*)*");
	private static final Map<String, String> TYPES = Map.of(
			"html", "text/html; charset=utf-8",
			"js", "text/javascript; charset=utf-8",
			"css", "text/css; charset=utf-8",
			"svg", "image/svg+xml",
			"png", "image/png",
			"ico", "image/x-icon");

	private StaticFiles()
	{
	}

	static void serve(HttpExchange ex) throws IOException
	{
		if (!"GET".equals(ex.getRequestMethod()))
		{
			ex.getResponseHeaders().set("Allow", "GET");
			HttpIo.sendError(ex, 405, "Method not allowed");
			return;
		}
		String name = ex.getRequestURI().getPath();
		if (name == null || name.equals("/"))
		{
			name = "/index.html";
		}
		name = name.substring(1);
		if (!SAFE_PATH.matcher(name).matches() || name.contains("..") || !name.contains("."))
		{
			notFound(ex);
			return;
		}
		String type = TYPES.get(name.substring(name.lastIndexOf('.') + 1));
		if (type == null)
		{
			notFound(ex);
			return;
		}
		try (InputStream in = StaticFiles.class.getResourceAsStream("/web/" + name))
		{
			if (in == null)
			{
				notFound(ex);
				return;
			}
			HttpIo.send(ex, 200, type, in.readAllBytes());
		}
	}

	private static void notFound(HttpExchange ex) throws IOException
	{
		HttpIo.send(ex, 404, "text/plain; charset=utf-8", "Not found".getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
