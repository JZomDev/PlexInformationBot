package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;

public class PlexConnection extends PlexBaseItem {
	private String protocol;
	private String address;
	private Integer port;
	private String uri;
	private Integer local;

	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public URI uri() {
		try {
			return new URI(getUri());
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	
	public Integer getLocal() {
		return local;
	}

	public void setLocal(Integer local) {
		this.local = local;
	}
}