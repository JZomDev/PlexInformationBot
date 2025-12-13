package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class PlexAuthorizer {
	private static final URI URI_PIN_REQUEST;
	private static final String URI_TEMPLATE_PIN_VERIFICATION = "https://plex.tv/pins/{pinId}.xml";

	static {
		try {
			URI_PIN_REQUEST = new URI("https://plex.tv/pins.xml");
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown exception. See attached stacktrace", e);
		}
	}

	private final PlexHTTPClient client;

	PlexAuthorizer(PlexHTTPClient client) {
		this.client = Objects.requireNonNull(client);
	}

	public PlexPin requestAuthenticationPin() {
		return client.requestPin(URI_PIN_REQUEST);
	}

	public PlexPin verify(PlexPin pin) {
		URI uri;
		try {
			uri = new URI(URI_TEMPLATE_PIN_VERIFICATION.replace("{pinId}", Integer.toString(pin.getId())));
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown exception. See attached stacktrace", e);
		}
		return client.verifyPin(uri);
	}
}
