package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

public class PlexTranscode extends PlexServerMediaContainer {
	public PlexTranscode(PlexMediaServer server) throws URISyntaxException {
		super(new URIBuilder(server.getUri()).appendPath("transcode").build(), server);
	}

	public List<PlexTranscodeSession> sessions() {
		URI uri;
		try {
			uri = new URIBuilder(getUri()).appendPath("sessions").build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
		return new PlexGeneralPurposeMediaContainer<PlexMetadata, PlexDirectory>(uri, server()).getTranscodeSessions();
	}

	public void killSession(PlexTranscodeSession session) {
		getClient().delete(session.key(), getToken(), Optional.empty());
	}
}
