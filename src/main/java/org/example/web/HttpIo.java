package org.example.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Small request and response helpers shared by the web handlers. */
final class HttpIo
{
	static final Gson GSON = new Gson();
	static final int MAX_BODY = 64 * 1024;

	/** Thrown when a request body is over the size limit. */
	static final class TooLarge extends Exception
	{
		TooLarge()
		{
			super("Request body too large");
		}
	}

	private HttpIo()
	{
	}

	static String readBody(HttpExchange ex) throws IOException, TooLarge
	{
		String length = ex.getRequestHeaders().getFirst("Content-Length");
		if (length != null)
		{
			try
			{
				if (Long.parseLong(length.trim()) > MAX_BODY)
				{
					throw new TooLarge();
				}
			}
			catch (NumberFormatException ignored)
			{
				// fall through to the bounded read below
			}
		}
		byte[] bytes = ex.getRequestBody().readNBytes(MAX_BODY + 1);
		if (bytes.length > MAX_BODY)
		{
			throw new TooLarge();
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

	static void sendJson(HttpExchange ex, int status, Object body) throws IOException
	{
		send(ex, status, "application/json; charset=utf-8", GSON.toJson(body).getBytes(StandardCharsets.UTF_8));
	}

	static void sendError(HttpExchange ex, int status, String message) throws IOException
	{
		sendJson(ex, status, Map.of("error", message));
	}

	static void send(HttpExchange ex, int status, String contentType, byte[] bytes) throws IOException
	{
		ex.getResponseHeaders().set("Content-Type", contentType);
		ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
		if (bytes.length > 0)
		{
			try (OutputStream out = ex.getResponseBody())
			{
				out.write(bytes);
			}
		}
		ex.close();
	}
}
