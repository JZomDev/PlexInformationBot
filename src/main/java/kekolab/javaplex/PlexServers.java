package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class PlexServers extends PlexMediaContainer {
	private static final URI URI;
	private static final String SERVER_URI_TEMPLATE = "https://plex.tv/api/servers/{machineIdentifier}";

	static {
		try {
			URI = new URI("https://plex.tv/api/servers");
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown exception. Please see attached stacktrace", e);
		}
	}

	@JsonProperty("Server")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<PlexServer> servers;

	public PlexServers(PlexHTTPClient client, String token) {
		super(URI, client, Optional.of(token));
		servers = new ArrayList<>();
	}

	public PlexServers(PlexHTTPClient client, String token, String machineIdentifier) throws URISyntaxException {
		super(new URI(SERVER_URI_TEMPLATE.replace("{machineIdentifier}", machineIdentifier)), client,
				Optional.of(token));
		servers = new ArrayList<>();
	}

	public List<PlexServer> getServers() {
		ensureFetched(servers);
		servers.forEach(s -> {
			((PlexServer) s).setClient(getClient());
			((PlexServer) s).setToken(getToken().get());
		});
		return servers;
	}

	public void setServers(List<PlexServer> servers) {
		this.servers = servers;
	}

	public List<PlexServer> list() {
		return getServers();
	}

	public PlexServer getFromMachineIdentifier(String machineIdentifier) {
		try {
			return new PlexServers(getClient(), getToken().get(), machineIdentifier).getServers().get(0);
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown error. Please see attached stacktrace", e);
		}
	}
}