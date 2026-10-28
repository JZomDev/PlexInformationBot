package org.example.announce;

import java.time.Instant;

/** One announceable movie or season; details is the pre-built details line. */
public record Item(String type, String key, String title, int year, String summary, String details,
		String posterUrl, Instant added)
{
}
