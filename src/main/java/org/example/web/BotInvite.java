package org.example.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Builds the "add the bot to a server" link from the bot token alone, without any network call. */
final class BotInvite
{
	static final long VIEW_CHANNELS = 1024L;
	static final long SEND_MESSAGES = 2048L;
	static final long EMBED_LINKS = 16384L;
	static final long READ_MESSAGE_HISTORY = 65536L;
	static final long ADD_REACTIONS = 64L;
	static final long MANAGE_ROLES = 268435456L;
	static final long PERMISSIONS = VIEW_CHANNELS + SEND_MESSAGES + EMBED_LINKS + READ_MESSAGE_HISTORY
			+ ADD_REACTIONS + MANAGE_ROLES;

	private BotInvite()
	{
	}

	/** The invite URL, or "" when the token does not start with a usable client id. The token is never returned. */
	static String url(String token)
	{
		String id = clientId(token);
		return id == null ? "" : "https://discord.com/oauth2/authorize?client_id=" + id
				+ "&scope=bot%20applications.commands&permissions=" + PERMISSIONS;
	}

	/** The first dot-separated part of a bot token is the base64 of the bot user id (the client id). */
	private static String clientId(String token)
	{
		if (token == null || token.isBlank())
		{
			return null;
		}
		String first = token.strip().split("\\.", 2)[0];
		try
		{
			String id = new String(Base64.getUrlDecoder().decode(first), StandardCharsets.UTF_8);
			return id.matches("[0-9]{17,20}") ? id : null;
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}
