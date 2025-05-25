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
import kekolab.javaplex.PlexEpisode;
import kekolab.javaplex.PlexMedia;
import kekolab.javaplex.PlexMediaServer;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexMovie;
import kekolab.javaplex.PlexPlayer;
import kekolab.javaplex.PlexSeason;
import kekolab.javaplex.PlexShow;
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
			if (mediatags == null || mediatags.isEmpty())
			{
				activtyTitle = "No current activity";
			}
			else
			{
				activtyTitle = "Current activity on " + plexMediaServer.getFriendlyName();
				Long total_bandwidth = 0L;
				for (int i = 0; i < mediatags.size(); i++)
				{
					PlexMediatag<?> plexMediatag = mediatags.get(i);
					if (plexMediatag instanceof PlexEpisode plexEpisode) {
						total_bandwidth += buildEpisodeString(stringBuilder, plexEpisode, i);
					} else if (plexMediatag instanceof PlexMovie plexMovie) {
						total_bandwidth += buildMovieString(stringBuilder, plexMovie, i);
					}
				}

				stringBuilder.append("\n");
				String bandwidthFormat = numberFormat.format((double) total_bandwidth / 1000);

				stringBuilder.append(mediatags.size() + (mediatags.size() == 1 ? " stream @ " : " streams @ ") + bandwidthFormat + "Mbps");
				embed.setDescription(stringBuilder.toString());
			}


			embed.setTitle(activtyTitle);
			embed.setFooter("Updated " + updatedTime);
			embed.setAuthor(api.getYourself());
			return embed;

		}, api.getThreadPool().getExecutorService());
	}

	private long buildEpisodeString(StringBuilder stringBuilder, PlexEpisode episode, int index) {
		PlexPlayer player = episode.getPlayer();
		PlexSeason season = episode.parent();
		String userID = episode.getUser().getId();
		String username = episode.getUser().getTitle();
		String friendlyName = "";
		if (!TAUTULLI_URL.isEmpty()) {
			try {
				friendlyName = getUsers().getOrDefault(userID, "");
			} catch (Exception e) {
				log.error(e);
			}
		}

		long bandwidth = 0;
		if (episode.getSession() == null)
		{
			return 0;
		}
		else
		{
			bandwidth = episode.getSession().getBandwidth();
		}
		PlexShow show = season.parent();
		String episodeTitle = episode.getTitle();
		String seasonNumber = String.valueOf(season.getIndex());
		String episodeNumber = String.valueOf(episode.getIndex());
		String grandparentTitle = show.getTitle();
		String state = player.getState();
		String stateStr = state.equals("paused") ? ":pause_button:" : ":arrow_forward:";
		String videoResolution = episode.getMedia().get(0).getVideoResolution();
		if (!videoResolution.endsWith("p") && !videoResolution.equals("SD") && !videoResolution.equals("4K")) {
			videoResolution += "p";
		}

		long bitrate = 0;
		for (PlexMedia pm : episode.getMedia())
			bitrate += pm.getBitrate();{
		}

		String qualityProfile = getQualityProfile(bandwidth, bitrate);
		String bandwidthFormat = numberFormat.format((double) bandwidth / 1000);

		long viewOffset = episode.getViewOffset();
		long duration = episode.getDuration();

		stringBuilder.append(index + 1).append(" - :tv: | ").append(stateStr)
			.append(" ").append(grandparentTitle)
			.append(" (S").append(seasonNumber).append(" E").append(episodeNumber).append(") -").append(episodeTitle).append("\n");

		stringBuilder.append(":eyes: ").append(username).append(!friendlyName.isEmpty() ? (" (" + friendlyName + ")") : "").append("\n");
		stringBuilder.append(":gear: ").append(qualityProfile).append("(").append(videoResolution).append(") | @").append(bandwidthFormat).append("Mbps").append("\n");

		appendTiming(stringBuilder, viewOffset, duration);
		stringBuilder.append("\n");
		return bandwidth;
	}

	private long buildMovieString(StringBuilder stringBuilder, PlexMovie movie, int index) {
		PlexPlayer player = movie.getPlayer();
		String userID = movie.getUser().getId();
		String username = movie.getUser().getTitle();
		String friendlyName = "";
		if (!TAUTULLI_URL.isEmpty()) {
			try {
				friendlyName = getUsers().getOrDefault(userID, "");
			} catch (Exception e) {
				log.error(e);
			}
		}
		long bandwidth = 0;
		if (movie.getSession() == null)
		{
			return 0;
		}
		else
		{
			bandwidth = movie.getSession().getBandwidth();
		}

		String fullTitle = movie.getTitle();
		String state = player.getState();
		String stateStr = state.equals("paused") ? ":pause_button:" : ":arrow_forward:";
		String videoResolution = movie.getMedia().get(0).getVideoResolution();
		if (!videoResolution.endsWith("p") && !videoResolution.equalsIgnoreCase("SD") && !videoResolution.equalsIgnoreCase("4K")) {
			videoResolution += "p";
		}
		long bitrate = 0;
		for (PlexMedia pm : movie.getMedia())
		{
			bitrate += pm.getBitrate();
		}
		String qualityProfile = getQualityProfile(bandwidth, bitrate);
		String bandwidthFormat = numberFormat.format((double) bandwidth / 1000);

		long viewOffset = movie.getViewOffset();
		long duration = movie.getDuration();

		stringBuilder.append(index + 1).append(" - :cinema: | ").append(stateStr).append(" ").append(fullTitle).append("\n");
		stringBuilder.append(":eyes: ").append(username).append(!friendlyName.isEmpty() ? (" (" + friendlyName + ")") : "").append("\n");
		stringBuilder.append(":gear: ").append(qualityProfile).append("(").append(videoResolution).append(") | @").append(bandwidthFormat).append("Mbps").append("\n");

		appendTiming(stringBuilder, viewOffset, duration);
		stringBuilder.append("\n");
		return bandwidth;
	}

	private void appendTiming(StringBuilder builder, long viewOffset, long duration) {
		ArrayList<Long> elapsedTime = getIntervalTime(viewOffset);
		ArrayList<Long> totalTime = getIntervalTime(duration);

		long elapsedHours = elapsedTime.get(0);
		long elapsedMinutes = elapsedTime.get(1);
		long elapsedSeconds = elapsedTime.get(2);
		long totalHours = totalTime.get(0);
		long totalMinutes = totalTime.get(1);
		long totalSeconds = totalTime.get(2);

		String elapsedStr = elapsedHours > 0 ?
			elapsedHours + ":" + numberFormat2.format(elapsedMinutes) + ":" + numberFormat2.format(elapsedSeconds) :
			elapsedMinutes + ":" + numberFormat2.format(elapsedSeconds);

		String totalStr = totalHours > 0 ?
			totalHours + ":" + numberFormat2.format(totalMinutes) + ":" + numberFormat2.format(totalSeconds) :
			totalMinutes + ":" + numberFormat2.format(totalSeconds);

		builder.append(":alarm_clock: ").append(elapsedStr).append(" / ").append(totalStr);
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

