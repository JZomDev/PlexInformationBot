package org.plexinfobot.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.plexinfobot.Main.API_KEY;
import static org.plexinfobot.Main.TAUTULLI_URL;

public class PlexFriendlyName
{
	private static final Logger log = LogManager.getLogger(PlexFriendlyName.class);
	HashMap<String, String> friendlyNames;

	public PlexFriendlyName()
	{
		friendlyNames = new HashMap<>();
	}

	private synchronized void setFriendlyNames(HashMap<String, String> friendlyNames)
	{
		this.friendlyNames = friendlyNames;
	}

	public synchronized String getName(String name)
	{
		boolean hasName = friendlyNames.containsKey(name);

		if (!hasName)
		{
			updateMap();
		}

		return friendlyNames.getOrDefault(name, "");
	}

	private synchronized void updateMap()
	{
		HashMap<String, String> mappedUsers = new HashMap<>();

		String apikey = API_KEY;
		String tautulliURL = TAUTULLI_URL;

		String urlStr = "http://" + tautulliURL + "/api/v2?apikey=" + apikey + "&cmd=get_activity";
		try
		{
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				String line;
				StringBuilder stringBuilder = new StringBuilder();
				while ((line = reader.readLine()) != null)
				{
					stringBuilder.append(line);
				}

				JsonElement root = JsonParser.parseString(stringBuilder.toString());
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

				setFriendlyNames(mappedUsers);
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
			}
			finally
			{
				if (connection != null)
				{
					connection.disconnect();
				}
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}
}
