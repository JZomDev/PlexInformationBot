package org.example.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import kekolab.javaplex.PlexEpisode;
import kekolab.javaplex.PlexMedia;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexMovie;
import kekolab.javaplex.PlexPlayer;
import kekolab.javaplex.PlexSeason;
import kekolab.javaplex.PlexSession;
import kekolab.javaplex.PlexShow;
import kekolab.javaplex.PlexUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import static org.example.Main.API_KEY;
import static org.example.Main.TAUTULLI_URL;

public class PlexInformationWorker
{
	private static final Logger log = LogManager.getLogger(PlexInformationWorker.class);
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	NumberFormat numberFormat = new DecimalFormat("#0.00");
	NumberFormat numberFormat2 = new DecimalFormat("#00");

	public CompletableFuture<EmbedBuilder> execute(DiscordApi api, PlexMediaServer plexMediaServer, List<PlexMediatag<?>> mediatags)
	{
		return CompletableFuture.supplyAsync(() -> {
			EmbedBuilder embed = new EmbedBuilder();
			String activtyTitle = "";
			Instant time = Instant.now();
			String updatedTime = formatter.format(Date.from(time));
			StringBuilder stringBuilder = new StringBuilder();
			int streamCount = mediatags.size();
			List<PlexShow> shows = null;
			if (streamCount == 0)
			{
				activtyTitle = "No current activity";
			}
			else
			{
				activtyTitle = "Current activity on " + plexMediaServer.getFriendlyName();
				long total_bandwidth = 0;
				for (int i = 0; i < mediatags.size(); i++)
				{
					PlexMediatag<?> plexMediatag = mediatags.get(i);
					PlexSession plexSession = plexMediatag.getSession();

					if (plexSession == null)
					{
						activtyTitle = "No current activity";
						continue;
					}
					PlexPlayer plexPlayer = plexMediatag.getPlayer();
					PlexUser plexUser = plexMediatag.getUser();

					long bandwidth = plexSession.getBandwidth();
					total_bandwidth += bandwidth;
					String full_title = "";
					String state = "";
					String media_type = "";
					String video_full_resolution = "";
					String quality_profile = null;
					String username = plexUser.getTitle();
					String userID = plexUser.getId();
					String friendly_name = "";
					if (!TAUTULLI_URL.isEmpty())
					{
						try
						{
							friendly_name = getUsers().getOrDefault(userID, "");
						}
						catch (Exception e)
						{
							log.error(e);
						}
					}
					long view_offset = 0;
					long stream_duration = 0;

					if (plexMediatag instanceof PlexMovie plexMovie)
					{
						view_offset = plexMovie.getViewOffset();
						stream_duration = plexMovie.getDuration();
						for (int j = 0; j < plexMovie.getMedia().size(); j++)
						{
							state = plexPlayer.getState();

							full_title = plexMovie.getTitle();
							media_type = plexMovie.getType();
							PlexMedia plexMedia = plexMovie.getMedia().get(j);
							video_full_resolution = plexMedia.getVideoResolution();
							if (!video_full_resolution.endsWith("p") && !video_full_resolution.equals("SD"))
							{
								video_full_resolution += "p";
							}
							long bitrate = plexMedia.getBitrate();
							quality_profile = getQualityProfile(bandwidth, bitrate);

						}
					}

					String title = plexMediatag.getTitle();// tv episode name
					String grandparent_title = "";
					String episodeNumber = "";
					String seasonNumber = "";
					if (plexMediatag instanceof PlexEpisode plexEpisode)
					{
						PlexSeason plexSeason = plexEpisode.parent();
						PlexShow plexShow = plexSeason.parent();
						episodeNumber = String.valueOf(plexEpisode.getIndex());
						seasonNumber = String.valueOf(plexSeason.getIndex());
						long bitrate = plexEpisode.getMedia().get(0).getBitrate();
						grandparent_title = plexShow.getTitle();

						view_offset = plexMediatag.getViewOffset();
						stream_duration = plexEpisode.getDuration();
						for (int j = 0; j < plexEpisode.getMedia().size(); j++)
						{
							state = plexPlayer.getState();

							full_title = plexMediatag.getTitle();
							media_type = plexMediatag.getType();

							PlexMedia plexMedia = plexEpisode.getMedia().get(j);
							video_full_resolution = plexMedia.getVideoResolution();
							if (!video_full_resolution.endsWith("p") && !video_full_resolution.equals("SD"))
							{
								video_full_resolution += "p";
							}
							quality_profile = getQualityProfile(bandwidth, bitrate);
						}
					}

					String bandwidthFormat = numberFormat.format((double) bandwidth / 1000);
					String stateStr = state.equals("paused") ? ":pause_button:" : ":arrow_forward:";

					String mediaChoice = media_type.equals("episode") ? ":tv:" : ":cinema:";
					if (mediaChoice.equals(":tv:"))
					{
						stringBuilder.append(i + 1).append(" - ").append(mediaChoice)
							.append(" | ").append(stateStr)
							.append(" ").append(grandparent_title)
							.append(" (S").append(seasonNumber).append(" E").append(episodeNumber).append(") -").append(title).append("\n");
					}
					else
					{
						stringBuilder.append(i + 1).append(" - ").append(mediaChoice).append(" | ").append(stateStr).append(" ").append(full_title).append("\n");
					}
					stringBuilder.append(":eyes: ").append(username + (!friendly_name.isEmpty() ? (" (" + friendly_name + ")") : "")).append("\n");
					stringBuilder.append(":gear: ").append(quality_profile).append("(").append(video_full_resolution).append(") | @").append(bandwidthFormat).append("Mbps").append("\n");

					ArrayList<Long> elapsedTime = getIntervalTime(view_offset);
					long elapsedHours = elapsedTime.get(0);
					long elapsedMinutes = elapsedTime.get(1);
					long elapsedSeconds = elapsedTime.get(2);

					ArrayList<Long> duraTime = getIntervalTime(stream_duration);

					long duraHours = duraTime.get(0);
					long duraMinutes = duraTime.get(1);
					long duraSeconds = duraTime.get(2);

					String elapsedTimeStr = elapsedHours > 0 ? elapsedHours + ":" + numberFormat2.format(elapsedMinutes) + ":" + numberFormat2.format(elapsedSeconds) : elapsedMinutes + ":" + numberFormat2.format(elapsedSeconds);
					String duraTimeStr = duraHours > 0 ? duraHours + ":" + numberFormat2.format(duraMinutes) + ":" + numberFormat2.format(duraSeconds) : duraMinutes + ":" + numberFormat2.format(duraSeconds);

					stringBuilder.append(":alarm_clock: ").append(elapsedTimeStr).append(" / ").append(duraTimeStr);

					stringBuilder.append("\n");
				}

				stringBuilder.append("\n");
				String bandwidthFormat = numberFormat.format((double) total_bandwidth / 1000);

				stringBuilder.append(streamCount + (streamCount == 1 ? " stream @ " : " streams @ ") + bandwidthFormat + "Mbps");


				embed.setDescription(stringBuilder.toString());
			}


			embed.setTitle(activtyTitle);
			embed.setFooter("Updated " + updatedTime);
			embed.setAuthor(api.getYourself());
			return embed;

		}, api.getThreadPool().getExecutorService());
	}

	private HashMap<String, String> getUsers() throws Exception
	{
		HashMap<String, String> mappedUsers = new HashMap<>();

		String apikey = API_KEY;
		String tautulliURL = TAUTULLI_URL;

		String urlStr = "http://" + tautulliURL + "/api/v2?apikey=" + apikey + "&cmd=get_activity";
		URL url = new URL(urlStr);
		URLConnection request = url.openConnection();
		request.connect();

		JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element

		root = root.getAsJsonObject().get("response");

		if (root.getAsJsonObject().get("result").getAsString().equals("success"))
		{
			JsonObject data = root.getAsJsonObject().get("data").getAsJsonObject();
			int streamCount = data.get("stream_count").getAsInt();

			if (streamCount > 0)
			{
				JsonArray streams = data.getAsJsonObject().get("sessions").getAsJsonArray();
				for (int i = 0; i < streams.size(); i++)
				{
					JsonObject stream = streams.get(i).getAsJsonObject();
					String friendlyName = stream.get("friendly_name").getAsString();
					String user_id = stream.get("user_id").getAsString();
					mappedUsers.put(user_id, friendlyName);
				}
			}
		}

		return mappedUsers;
	}

	public static String getQualityProfile(long streamBitrate, long sourceBitrate)
	{
		Integer minBitrate = null;
		for (Integer b : VideoQualityProfiles.VIDEO_QUALITY_PROFILES.keySet())
		{
			if (streamBitrate <= b && b <= sourceBitrate)
			{
				if (minBitrate == null || b < minBitrate)
				{
					minBitrate = b;
				}
			}
		}
		if (minBitrate != null)
		{
			return VideoQualityProfiles.VIDEO_QUALITY_PROFILES.get(minBitrate);
		}
		return "Original";
	}

	public static ArrayList<Long> getIntervalTime(long longInterval)
	{

		long intMillis = longInterval;
		long dd = TimeUnit.MILLISECONDS.toDays(intMillis);
		intMillis -= TimeUnit.DAYS.toMillis(dd);
		long hh = TimeUnit.MILLISECONDS.toHours(intMillis);
		intMillis -= TimeUnit.HOURS.toMillis(hh);
		long mm = TimeUnit.MILLISECONDS.toMinutes(intMillis);
		intMillis -= TimeUnit.MINUTES.toMillis(mm);
		long ss = TimeUnit.MILLISECONDS.toSeconds(intMillis);
		intMillis -= TimeUnit.SECONDS.toMillis(ss);

		ArrayList<Long> returnThis = new ArrayList<>();
		returnThis.add(hh);
		returnThis.add(mm);
		returnThis.add(ss);
		return returnThis;
	}
}

