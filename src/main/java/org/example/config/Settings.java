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
}
