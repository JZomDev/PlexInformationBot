package org.example.announce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.Settings;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;

import java.awt.Color;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

/** Posts an announcement card as a silent embed in the announcements channel. */
public final class DiscordCardSink implements CardSink
{
	static final int MAX_SUMMARY = 300;
	private static final Color PLEX_GOLD = new Color(0xE5A00D);
	private static final long SEND_TIMEOUT_SECONDS = 15;

	private final Logger logger = LogManager.getLogger(DiscordCardSink.class);
	static final int MAX_TITLE = 256;

	private final Function<Item, Future<?>> sender;
	private final long timeoutSeconds;

	/** The channel is looked up on every post so a changed setting applies at once. */
	public DiscordCardSink(Supplier<TextChannel> channel)
	{
		this(item -> new MessageBuilder()
				.setEmbed(embed(item))
				.setAllowedMentions(noMentions())
				.send(channel.get()), SEND_TIMEOUT_SECONDS);
	}

	DiscordCardSink(Function<Item, Future<?>> sender, long timeoutSeconds)
	{
		this.sender = sender;
		this.timeoutSeconds = timeoutSeconds;
	}

	DiscordCardSink(Function<Item, Future<?>> sender)
	{
		this(sender, SEND_TIMEOUT_SECONDS);
	}

	/** Cuts to at most max chars without splitting a surrogate pair. */
	static String truncate(String s, int max)
	{
		if (s.length() <= max)
		{
			return s;
		}
		int end = Character.isHighSurrogate(s.charAt(max - 1)) ? max - 1 : max;
		return s.substring(0, end);
	}

	public static DiscordCardSink forApi(DiscordApi api, Supplier<Settings> settings)
	{
		return new DiscordCardSink(() -> api.getTextChannelById(settings.get().announceChannelId()).orElseThrow());
	}

	static EmbedBuilder embed(Item item)
	{
		boolean movie = Announcer.MOVIE.equals(item.type());
		String title = item.title() == null ? "" : item.title();
		if (movie && item.year() > 0)
		{
			title = title + " (" + item.year() + ")";
		}
		EmbedBuilder e = new EmbedBuilder()
				.setColor(PLEX_GOLD)
				.setAuthor(movie ? "New movie on Plex" : "Season complete on Plex")
				.setTitle(truncate(title, MAX_TITLE));
		String summary = item.summary();
		if (summary != null && !summary.isBlank())
		{
			e.setDescription(truncate(summary, MAX_SUMMARY));
		}
		if (item.details() != null && !item.details().isBlank())
		{
			e.setFooter(item.details());
		}
		if (item.posterUrl() != null && !item.posterUrl().isBlank())
		{
			e.setThumbnail(item.posterUrl());
		}
		return e;
	}

	/** Nothing in an announcement may ping anyone. */
	static AllowedMentions noMentions()
	{
		return new AllowedMentionsBuilder().build();
	}

	@Override
	public boolean post(Item item)
	{
		try
		{
			sender.apply(item).get(timeoutSeconds, TimeUnit.SECONDS);
			return true;
		}
		catch (InterruptedException e)
		{
			// The send was already fired, so Discord may have accepted it: report it posted, not failed.
			Thread.currentThread().interrupt();
			return true;
		}
		catch (TimeoutException e)
		{
			logger.warn("Announcement send was not confirmed in time; treating it as posted.");
			return true;
		}
		catch (Exception e)
		{
			logger.warn("Could not post an announcement: {}", e.getClass().getSimpleName());
			return false;
		}
	}
}
