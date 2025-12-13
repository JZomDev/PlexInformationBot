package kekolab.javaplex;

import java.net.URI;

public class PlexServerMediaContainer extends PlexMediaContainer {
	private PlexMediaServer server;

	public PlexServerMediaContainer(URI uri, PlexMediaServer server) {
		super(uri, server.getClient(), server.getToken());
		this.server = server;
	}

	PlexMediaServer server() {
		return server;
	}
}
