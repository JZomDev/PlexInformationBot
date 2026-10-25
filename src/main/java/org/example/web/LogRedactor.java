package org.example.web;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** Removes secrets from log text before it leaves the server. */
final class LogRedactor
{
	static final String MASK = "****";

	/** key=value, key: value, "key":"value" for token / api key names (also inside X-Plex-Token and botToken). */
	private static final Pattern KEYED = Pattern.compile(
			"(?i)((?:x-plex-token|x-api-key|api[_-]?key|apikey|token)[\"']?\\s*[=:]\\s*[\"']?)[^\\s&\"',;]+");
	private static final Pattern AUTH = Pattern.compile("(?i)(authorization[\"']?\\s*[=:]\\s*[\"']?)[^\\r\\n\"']+");

	private LogRedactor()
	{
	}

	static String redact(String text, Collection<String> secrets)
	{
		String out = KEYED.matcher(text).replaceAll("$1" + MASK);
		out = AUTH.matcher(out).replaceAll("$1" + MASK);
		List<String> sorted = secrets.stream().filter(s -> s != null && !s.isBlank())
				.sorted(Comparator.comparingInt(String::length).reversed()).toList();
		for (String secret : sorted)
		{
			out = out.replace(secret, MASK);
		}
		return out;
	}
}
