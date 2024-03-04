package org.example.listeners;

import com.google.gson.JsonArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.example.Main;
import static org.example.Main.PLEX_KEY;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DMListener implements MessageCreateListener
{
	String machineID;
	JsonArray sections;
	DiscordApi api;
	long userID;
	Timer timer;

	public DMListener(long userID, String machineID, DiscordApi api) throws Exception
	{
		this.machineID = machineID;
		this.sections = getPlexSections();
		this.api = api;
		this.userID = userID;
		getSharedServers();
		timer = new Timer();
		timer.schedule(new StopListener(this), Date.from(Instant.now().plus(86400, ChronoUnit.SECONDS)));
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event)
	{
		if (!event.isPrivateMessage())
		{
			return;
		}

		if (event.getMessage().getUserAuthor().get().getId() == api.getYourself().getId())
		{
			return;
		}

		String msg = event.getMessageContent();
		if (validEmail(msg))
		{
			event.getChannel().sendMessage("Got it we will be adding your email to plex shortly!");
			try
			{
				boolean success = plexInvite(msg);

				if (success)
				{
					event.getChannel().sendMessage("You have Been Added To Plex! Login to plex and accept the invite!");
					event.getApi().removeListener(this);
				}
				else
				{
					event.getChannel().sendMessage("There was an error adding this email address. Message Server Admin.");
				}
			}
			catch (Exception e)
			{
				event.getChannel().sendMessage("There was an error, please contact the server administrator");
			}
		}
		else
		{
			event.getChannel().sendMessage("Invalid email. Please just type in your email and nothing else.");
		}
	}

	private boolean plexInvite(String invitedEmail) throws Exception
	{
		// Create an instance of HttpClient
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		// Create an instance of HttpPost with the desired URL
		String postUrl = "https://plex.tv/api/servers/" + machineID + "/shared_servers?X-Plex-Token=" + PLEX_KEY;
		HttpPost httpPost = new HttpPost(postUrl);

		// Add headers to the request
		httpPost.setHeader("Content-type", "application/json");

		String request = "{" +
			"  \"server_id\": \"\"," +
			"  \"shared_server\": {\"library_section_ids\" : " + sections.toString() + ", \"invited_email\": \"" + invitedEmail + "\"}," +
			"  \"sharing_settings\":" +
			"  {" +
			"    \"allowSync\": \"0\"," +
			"    \"allowCameraUpload\": \"0\"," +
			"    \"allowChannels\": \"0\"," +
			"    \"filterMovies\": {}," +
			"    \"filterTelevision\": {}," +
			"    \"filterMusic\": {}" +
			"  }" +
			"}";

		// Set the request body
		StringEntity entity = new StringEntity(request);
		httpPost.setEntity(entity);

		// Execute the request and obtain the response
		HttpResponse httpResponse = httpClient.execute(httpPost);

		// Extract the response's content
		int responseCode = httpResponse.getStatusLine().getStatusCode();

		return responseCode == 200;
	}

	private JsonArray getPlexSections() throws Exception
	{
		URL url = new URL("https://plex.tv/api/servers/" + machineID);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.addRequestProperty("X-Plex-Token", PLEX_KEY);
		request.addRequestProperty("Content-Type", "application/json");
		request.setRequestMethod("GET");

		request.setReadTimeout(5000);
		int status = request.getResponseCode();

		if (status == 200)
		{
			BufferedReader in = new BufferedReader(
				new InputStreamReader(request.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
			{
				content.append(inputLine);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			JsonArray root = new JsonArray();

			try
			{
				builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(new StringReader(content.toString())));
				document.getDocumentElement().normalize();
				NodeList nodeList = document.getElementsByTagName("Section");
				for (int i = 0; i < nodeList.getLength(); i++)
				{
					Node sectionProps = nodeList.item(i);

					String type = sectionProps.getAttributes().getNamedItem("type").getNodeValue();
					String value = sectionProps.getAttributes().getNamedItem("id").getNodeValue();
					if (type.equals("movie"))
					{
						root.add(value);
					}
					else if (type.equals("show"))
					{
						root.add(value);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			in.close();
			return root;
		}

		return new JsonArray();
	}

	private void getSharedServers() throws Exception
	{
		URL url = new URL("https://plex.tv/api/servers/" + machineID + "/shared_servers");//?X-Plex-Token=" + PLEX_KEY);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.addRequestProperty("X-Plex-Token", PLEX_KEY);
		request.addRequestProperty("Content-Type", "application/json");
		request.setRequestMethod("GET");

		request.setReadTimeout(5000);
		int status = request.getResponseCode();

		if (status == 200)
		{
			BufferedReader in = new BufferedReader(
				new InputStreamReader(request.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
			{
				content.append(inputLine);
			}
		}
	}

	private boolean validEmail(String emailAddress)
	{
		return Main.PATTERN_MATCH.matcher(emailAddress).matches();
	}

	class StopListener extends TimerTask
	{
		DMListener dmListener;

		public StopListener(DMListener dmListener)
		{
			this.dmListener = dmListener;
		}

		@Override
		public void run()
		{
			api.removeListener(dmListener);
			api.getUserById(userID).join().sendMessage("Timed Out. Message Server Admin with your email so They Can Add You Manually.");
		}
	}
}
