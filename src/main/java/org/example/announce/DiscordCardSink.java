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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Posts an announcement card as a silent embed in the announcements channel. */
public final class DiscordCardSink implements CardSink
{
	static final int MAX_SUMMARY = 300;
	private static final Color PLEX_GOLD = new Color(0xE5A00D);
	private static final long SEND_TIMEOUT_SECONDS = 15;

	private final Logger logger = LogManager.getLogger(DiscordCardSink.class);
	private final Supplier<TextChannel> channel;

	/** The channel is looked up on every post so a changed setting applies at once. */
	public DiscordCardSink(Supplier<TextChannel> channel)
	{
		this.channel = channel;
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
				.setTitle(title);
		String summary = item.summary();
		if (summary != null && !summary.isBlank())
		{
			e.setDescription(summary.length() > MAX_SUMMARY ? summary.substring(0, MAX_SUMMARY) : summary);
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
			new MessageBuilder()
					.setEmbed(embed(item))
					.setAllowedMentions(noMentions())
					.send(channel.get())
					.get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return true;
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return false;
		}
		catch (Exception e)
		{
			logger.warn("Could not post an announcement: {}", e.getClass().getSimpleName());
			return false;
		}
	}
}
