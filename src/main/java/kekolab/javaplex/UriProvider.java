package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import org.apache.hc.core5.net.URIBuilder;

class UriProvider {
	private Object value;
	private Supplier<URI> baseUri;

	/**
	 * 
	 * @param baseUri. if null, the value will be returned as uri
	 */
	UriProvider(Supplier<URI> baseUri) {
		this.baseUri = baseUri;
	}

	void setValue(Object value) {
		this.value = value;
	}

	Object getValue() {
		return value;
	}

	URI uri() {
		if (value == null)
			return null;

		if (baseUri == null) {
			try {
				return new URI((String) value);
			} catch (URISyntaxException e) {
				throw new PlexException(e);
			}
		}

		URIBuilder uri = new URIBuilder(baseUri.get()).clearParameters().removeQuery();
		String path = null;
		if (value instanceof String)
			path = (String) value;
		else if (value instanceof Integer)
			path = Integer.toString((int) value);
		else
			throw new PlexException(new URISyntaxException(value.toString(), "field must be a String or an Integer"));
		if (path.startsWith("/"))
			uri.setPath(path);
		else
			uri.appendPath(path);
		try {
			return uri.build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}
}