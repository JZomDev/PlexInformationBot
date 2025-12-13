package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import kekolab.javaplex.filtering.PlexFilter;

public class GenericCollectionsHelper<M extends PlexMetadata> {
	private final M target;
	private PlexMediaServer server;

	protected static URI uriParameter(PlexMediaServer server, PlexMediatag<?> mediatag) {
		try {
			return new URIBuilder().setScheme("server").setHost(server.getMachineIdentifier())
					.appendPath("com.plexapp.plugins.library").appendPath("library").appendPath("metadata")
					.appendPath(Integer.toString(mediatag.getRatingKey())).build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	protected static URI uriParameter(PlexMediaServer server, PlexSection section, PlexFilter filter) {
		try {
			return uriParameterBuilder(server, section, filter).build();
		} catch (URISyntaxException e) {
			throw new PlexException(e); // TODO
		}
	}

	protected static URI uriParameter(PlexMediaServer server, PlexSection section, PlexFilter filter, String sort) {
		try {
			return uriParameterBuilder(server, section, filter).addParameter(new BasicNameValuePair("sort", sort))
					.build();
		} catch (URISyntaxException e) {
			throw new PlexException(e); // TODO
		}
	}

	private static URIBuilder uriParameterBuilder(PlexMediaServer server, PlexSection section, PlexFilter filter) {
		return new URIBuilder().setScheme("server")
				.setHost(server.getMachineIdentifier())
				.appendPath("com.plexapp.plugins.library").appendPath("library").appendPath("sections")
				.appendPath(section.getKey())
				.appendPath("all")
				.addParameters(filter.getQueryParameters());
	}

	protected GenericCollectionsHelper(M target, PlexMediaServer server) {
		if (!(target instanceof PlexPlaylist || target instanceof PlexCollection))
			throw new PlexException("The Generic Collections Helper works only with playlists and collections");
		this.target = target;
		this.server = server;
	}

	protected M add(PlexMediatag<?> mediatag) {
		URI uri;
		try {
			uri = new URIBuilder(target.key()).addParameter("uri", uriParameter(server, mediatag).toString()).build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}

		server.getClient().put(uri, server.getToken(), Optional.empty());
		PlexGeneralPurposeMediaContainer<M, ?> container = new PlexGeneralPurposeMediaContainer<>(target.ratingKey(), server);
		return container.getMetadata().get(0);
	}

	M remove(URI uri) {
		PlexGeneralPurposeMediaContainer<M, ?> container = new PlexGeneralPurposeMediaContainer<>(uri, server);
		return server.getClient().delete(server.getToken(), container).getMetadata().get(0);
	}
}
