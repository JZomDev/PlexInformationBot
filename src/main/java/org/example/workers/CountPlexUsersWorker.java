package org.example.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import org.example.Main;
import org.javacord.api.DiscordApi;

public class CountPlexUsersWorker
{
	public CompletableFuture<String> execute(DiscordApi api, String PLEX_KEY)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				try
				{
					URL url = new URL("http://" + Main.IP + ":" + Main.PORT + "/status/sessions");
					URLConnection request = url.openConnection();
					request.addRequestProperty("X-Plex-Token", PLEX_KEY);
					request.addRequestProperty("Accept", "application/json");
					request.connect();
					JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

					JsonObject mediaContainer = root.getAsJsonObject().get("MediaContainer").getAsJsonObject();
					int transcodeCount = 0;
					int total = 0;
					if (mediaContainer.has("size") && mediaContainer.get("size").getAsLong() > 0)
					{
						JsonArray metadata = mediaContainer.get("Metadata").getAsJsonArray();
						total = metadata.size();
						for (int i = 0; i < metadata.size(); i++)
						{
							JsonArray media = metadata.get(i).getAsJsonObject().get("Media").getAsJsonArray();
							JsonObject members = media.get(0).getAsJsonObject();
							JsonArray part = members.get("Part").getAsJsonArray();
							for (int ii = 0; ii < part.size(); ii++)
							{
								JsonObject p = part.get(ii).getAsJsonObject();
								if (p.has("Stream"))
								{
									JsonArray stream = p.get("Stream").getAsJsonArray();
									for (int iii = 0; iii < stream.size(); iii++)
									{
										JsonObject streamObject = stream.get(iii).getAsJsonObject();

										if (streamObject.has("width"))
										{
											if (streamObject.has("decision") && streamObject.get("decision").getAsString().equals("transcode"))
											{
												transcodeCount++;
											}
										}
									}
								}
							}
						}
					}

					String line = "unknown";
					if (total == 0)
					{
						line = "to 0 people";
					}
					else if (total == 1)
					{
						line = "to 1 person";
					}
					else if (total > 1)
					{
						line = "to " + total + " people";
					}
					return line;
				}
				catch (Throwable t)
				{
					return "Unknown";
				}
			}
			catch (Exception e)
			{
				return "Unknown";
			}
		}, api.getThreadPool().getExecutorService());
	}
}
