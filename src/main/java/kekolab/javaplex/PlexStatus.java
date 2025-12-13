package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;



public class PlexStatus extends PlexServerMediaContainer  {
	public PlexStatus(PlexMediaServer server) throws URISyntaxException {
		super(new URIBuilder(server.getUri()).appendPath("status").build(), server);
	}

	public List<PlexMediatag<?>> sessions() {
		URI uri;
		try {
			uri = new URIBuilder(getUri()).appendPath("sessions").build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
		return new PlexGeneralPurposeMediaContainer<PlexMediatag<?>, PlexDirectory>(uri, server()).getMetadata();
	}
}
