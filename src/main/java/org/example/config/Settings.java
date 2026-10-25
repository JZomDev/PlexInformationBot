package org.example.config;

import java.util.Map;

public record Settings(String botToken, String plexIp, String plexPort, String plexToken,
					   String tautulliUrl, String tautulliApiKey, String radarrUrl, String radarrApiKey,
					   String sonarrUrl, String sonarrApiKey, String roleId, String roleChannelId,
					   String roleMessageId, String statusChannelId, String statusMessageId,
					   String queueChannelId, String queueMessageId)
{
	public static final String MASK = "********";

	public Settings
	{
		botToken = nz(botToken);
		plexIp = nz(plexIp);
		plexPort = nz(plexPort);
		plexToken = nz(plexToken);
		tautulliUrl = nz(tautulliUrl);
		tautulliApiKey = nz(tautulliApiKey);
		radarrUrl = nz(radarrUrl);
		radarrApiKey = nz(radarrApiKey);
		sonarrUrl = nz(sonarrUrl);
		sonarrApiKey = nz(sonarrApiKey);
		roleId = nz(roleId);
		roleChannelId = nz(roleChannelId);
		roleMessageId = nz(roleMessageId);
		statusChannelId = nz(statusChannelId);
		statusMessageId = nz(statusMessageId);
		queueChannelId = nz(queueChannelId);
		queueMessageId = nz(queueMessageId);
	}

	private static String nz(String value)
	{
		return value == null ? "" : value;
	}

	public static Settings empty()
	{
		return new Settings("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
	}

	public static Settings fromEnv(Map<String, String> env)
	{
		return new Settings(
				env.get("BOT_TOKEN"), env.get("IP"), env.get("PORT"), env.get("PLEX_KEY"),
				env.get("TAUTULLI_URL"), env.get("API_KEY"), env.get("RADARR_URL"), env.get("RADARR_API"),
				env.get("SONARR_URL"), env.get("SONARR_API"), env.get("ROLE_ID"), "", "",
				env.get("TEXT_CHANNELID"), env.get("MESSAGEID"),
				env.get("QUEUE_TEXT_CHANNELID"), env.get("QUEUEMESSAGEID"));
	}

	private static String pick(String file, String base)
	{
		return file.isEmpty() ? base : file;
	}

	/** Non-empty fields of fileValues win over this. */
	public Settings overlay(Settings f)
	{
		return new Settings(
				pick(f.botToken, botToken), pick(f.plexIp, plexIp), pick(f.plexPort, plexPort),
				pick(f.plexToken, plexToken), pick(f.tautulliUrl, tautulliUrl),
				pick(f.tautulliApiKey, tautulliApiKey), pick(f.radarrUrl, radarrUrl),
				pick(f.radarrApiKey, radarrApiKey), pick(f.sonarrUrl, sonarrUrl),
				pick(f.sonarrApiKey, sonarrApiKey), pick(f.roleId, roleId),
				pick(f.roleChannelId, roleChannelId), pick(f.roleMessageId, roleMessageId),
				pick(f.statusChannelId, statusChannelId), pick(f.statusMessageId, statusMessageId),
				pick(f.queueChannelId, queueChannelId), pick(f.queueMessageId, queueMessageId));
	}

	private static String mask(String secret)
	{
		return secret.isEmpty() ? "" : MASK;
	}

	public Settings masked()
	{
		return new Settings(mask(botToken), plexIp, plexPort, mask(plexToken), tautulliUrl,
				mask(tautulliApiKey), radarrUrl, mask(radarrApiKey), sonarrUrl, mask(sonarrApiKey),
				roleId, roleChannelId, roleMessageId, statusChannelId, statusMessageId,
				queueChannelId, queueMessageId);
	}

	private static String keep(String submitted, String saved)
	{
		return MASK.equals(submitted) ? saved : submitted;
	}

	/** this is the saved file state; a submitted MASK keeps the saved secret. */
	public Settings applyForm(Settings s)
	{
		return new Settings(keep(s.botToken, botToken), s.plexIp, s.plexPort, keep(s.plexToken, plexToken),
				s.tautulliUrl, keep(s.tautulliApiKey, tautulliApiKey), s.radarrUrl,
				keep(s.radarrApiKey, radarrApiKey), s.sonarrUrl, keep(s.sonarrApiKey, sonarrApiKey),
				s.roleId, s.roleChannelId, s.roleMessageId, s.statusChannelId, s.statusMessageId,
				s.queueChannelId, s.queueMessageId);
	}

	private static String host(String value)
	{
		String v = value.strip();
		if (v.regionMatches(true, 0, "http://", 0, 7))
		{
			v = v.substring(7);
		}
		else if (v.regionMatches(true, 0, "https://", 0, 8))
		{
			v = v.substring(8);
		}
		v = v.strip();
		int end = v.length();
		while (end > 0 && v.charAt(end - 1) == '/')
		{
			end--;
		}
		return v.substring(0, end).strip();
	}

	/** Host fields are stored as host:port; the runtime adds the scheme itself. Secrets are untouched. */
	public Settings normalisedHosts()
	{
		return new Settings(botToken, host(plexIp), plexPort, plexToken, host(tautulliUrl), tautulliApiKey,
				host(radarrUrl), radarrApiKey, host(sonarrUrl), sonarrApiKey, roleId, roleChannelId,
				roleMessageId, statusChannelId, statusMessageId, queueChannelId, queueMessageId);
	}

	private static final String BAD_ID = " must be the number Discord shows, 17 to 20 digits";

	/** A Discord snowflake: 17 to 20 ASCII digits (after trimming). */
	private static boolean snowflake(String value)
	{
		String v = value.strip();
		if (v.isEmpty())
		{
			return true;
		}
		if (v.length() < 17 || v.length() > 20)
		{
			return false;
		}
		for (int i = 0; i < v.length(); i++)
		{
			char c = v.charAt(i);
			if (c < '0' || c > '9')
			{
				return false;
			}
		}
		return true;
	}

	/** The fixed error text for the first Discord ID field that is neither empty nor a snowflake. */
	public java.util.Optional<String> invalidIdField()
	{
		String[] labels = {"Role ID", "Role channel ID", "Role message ID", "Status channel ID",
				"Status message ID", "Queue channel ID", "Queue message ID"};
		String[] values = {roleId, roleChannelId, roleMessageId, statusChannelId, statusMessageId,
				queueChannelId, queueMessageId};
		for (int i = 0; i < labels.length; i++)
		{
			if (!snowflake(values[i]))
			{
				return java.util.Optional.of(labels[i] + BAD_ID);
			}
		}
		return java.util.Optional.empty();
	}

	/** Surrounding whitespace removed from the Discord ID fields only. */
	public Settings trimmedIds()
	{
		return new Settings(botToken, plexIp, plexPort, plexToken, tautulliUrl, tautulliApiKey, radarrUrl,
				radarrApiKey, sonarrUrl, sonarrApiKey, roleId.strip(), roleChannelId.strip(), roleMessageId.strip(),
				statusChannelId.strip(), statusMessageId.strip(), queueChannelId.strip(), queueMessageId.strip());
	}
}
