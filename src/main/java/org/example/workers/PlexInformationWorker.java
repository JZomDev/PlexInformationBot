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
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.example.Main.API_KEY;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class PlexInformationWorker
{
	private static final Logger log = LogManager.getLogger(PlexInformationWorker.class);
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	NumberFormat numberFormat = new DecimalFormat("#0.00");
	NumberFormat numberFormat2 = new DecimalFormat("#00");

	public CompletableFuture<EmbedBuilder> execute(DiscordApi api)
	{
		return CompletableFuture.supplyAsync(() -> {
			EmbedBuilder embed = new EmbedBuilder();
			String title = "";
			Instant time = Instant.now();
			String updatedTime = formatter.format(Date.from(time));
			try
			{
				String apikey = API_KEY;
				String urlStr = "http://192.168.1.132:8181/api/v2?apikey=" + apikey + "&cmd=get_activity";
				URL url = new URL(urlStr);
				URLConnection request = url.openConnection();
				request.connect();

				JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element

				root = root.getAsJsonObject().get("response");

				if (root.getAsJsonObject().get("result").getAsString().equals("success"))
				{
					JsonObject data = root.getAsJsonObject().get("data").getAsJsonObject();
					int streamCount = data.get("stream_count").getAsInt();
					int total_bandwidth = data.get("total_bandwidth").getAsInt();
					StringBuilder stringBuilder = new StringBuilder();
					if (streamCount == 0)
					{
						title = "No current activity";
					}
					else
					{
						title = "Current activity on Redrum's Ark";
						JsonArray streams = data.getAsJsonObject().get("sessions").getAsJsonArray();
						for (int i = 0; i < streams.size(); i++)
						{
							JsonObject stream = streams.get(i).getAsJsonObject();
							int view_offset = stream.get("view_offset").getAsInt();
							int stream_duration = stream.get("stream_duration").getAsInt();
							String state = stream.get("state").getAsString();
							String friendlyName = stream.get("friendly_name").getAsString();
							String full_title = stream.get("full_title").getAsString();
							String media_type = stream.get("media_type").getAsString();
							String quality_profile = stream.get("quality_profile").getAsString();
							String video_full_resolution = stream.get("video_full_resolution").getAsString();
							int bandwidth = 0; // Mbps
							try
							{
								bandwidth = stream.get("bandwidth").getAsInt();
							}
							catch (Exception ignored)
							{

							}
							String bandwidthFormat = numberFormat.format((double) bandwidth / 1000);
							String stateStr = state.equals("paused") ? ":pause_button:" : ":arrow_forward:";

							String mediaChoice = media_type.equals("episode") ? ":tv:" : ":cinema:";
							stringBuilder.append(String.valueOf(i + 1)).append(" - ").append(mediaChoice).append(" | ").append(stateStr).append(" ").append(full_title).append("\n");
							stringBuilder.append(":eyes: ").append(friendlyName).append("\n");
							stringBuilder.append(":gear: ").append(quality_profile).append("(").append(video_full_resolution).append(") | @").append(bandwidthFormat).append("Mbps").append("\n");

							long millis1 = view_offset;
							long elapsedMinutes = (millis1 / 1000)  / 60;
							int elapsedSeconds = (int)((millis1 / 1000) % 60);


							long millis2 = stream_duration;
							long duraMinutes = (millis2 / 1000)  / 60;
							int duraSeconds = (int)((millis2 / 1000) % 60);

							stringBuilder.append(":alarm_clock: ").append(elapsedMinutes).append(":").append(numberFormat2.format(elapsedSeconds)).append(" / ").append(duraMinutes).append(":").append(numberFormat2.format(duraSeconds));

							stringBuilder.append("\n");

						}

						stringBuilder.append("\n");
						String bandwidthFormat = numberFormat.format((double) total_bandwidth / 1000);

						stringBuilder.append(streamCount + (streamCount == 1 ? " stream @ " : " streams @ ") + bandwidthFormat + "Mbps");


						embed.setDescription(stringBuilder.toString());
					}
				}
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
			}
			embed.setTitle(title);
			embed.setFooter("Updated " + updatedTime);
			embed.setAuthor(api.getYourself());
			return embed;

			}, api.getThreadPool().getExecutorService());
	}
}
