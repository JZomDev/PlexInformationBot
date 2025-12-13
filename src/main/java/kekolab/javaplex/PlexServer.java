package kekolab.javaplex;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import kekolab.javaplex.mappers.BooleanDeserializer;
import kekolab.javaplex.mappers.TimestampDeserializer;

public class PlexServer extends PlexBaseItem {
	private String accessToken;
	private String name;
	private String address;
	private Integer port;
	private String version;
	private String scheme;
	private String host;
	private String localAddresses;
	private String machineIdentifier;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date createdAt;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date updatedAt;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean owned;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean synced;
	@JsonProperty("Section")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<PlexServer.Section> sections;

	@JsonIgnore
	private PlexHTTPClient client;
	@JsonIgnore
	private String token;
	@JsonIgnore
	private boolean detailed;

	public PlexServer() {
		detailed = false;
		sections = new ArrayList<>();
	}

	void setToken(String token) {
		this.token = token;
	}

	void setClient(PlexHTTPClient client) {
		this.client = client;
	}

	private boolean isDetailed() {
		return detailed;
	}

	private void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	void ensureDetailed(Object field) {
		if (!isDetailed() && (field == null || field instanceof Collection collection && collection.isEmpty())) {
			try {
				PlexServer detailed = (PlexServer) new PlexServers(client, token, getMachineIdentifier()).list().get(0);
				detailed.setDetailed(true);
				update(detailed);
				setDetailed(true);
			} catch (URISyntaxException e) {
				throw new PlexException("Unknown error. Please see attached stacktrace", e);
			}
		}
	}

	void update(PlexServer server) {
		setAccessToken(server.getAccessToken());
		setAddress(server.getAddress());
		setCreatedAt(server.getCreatedAt());
		setHost(server.getHost());
		setLocalAddresses(server.getLocalAddresses());
		setMachineIdentifier(server.getMachineIdentifier());
		setName(server.getName());
		setOwned(server.getOwned());
		setPort(server.getPort());
		setScheme(server.getScheme());
		setSections(server.getSections());
		setSynced(server.getSynced());
		setUpdatedAt(server.getUpdatedAt());
		setVersion(server.getVersion());
	}

	public List<PlexServer.Section> getSections() {
		ensureDetailed(sections);
		return sections;
	}

	public PlexServerShares serverShares() {
		try {
			return new PlexServerShares(client, token, getMachineIdentifier());
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown error. Please see attached stacktrace", e);
		}
	}

	public void setSections(List<PlexServer.Section> sections) {
		this.sections = sections;
	}

	public String getAccessToken() {
		ensureDetailed(accessToken);
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getName() {
		ensureDetailed(name);
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		ensureDetailed(port);
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getVersion() {
		ensureDetailed(version);
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScheme() {
		ensureDetailed(scheme);
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHost() {
		ensureDetailed(host);
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getLocalAddresses() {
		ensureDetailed(localAddresses);
		return localAddresses;
	}

	public void setLocalAddresses(String localAddresses) {
		this.localAddresses = localAddresses;
	}

	public String getMachineIdentifier() {
		ensureDetailed(machineIdentifier);
		return machineIdentifier;
	}

	public void setMachineIdentifier(String machineIdentifier) {
		this.machineIdentifier = machineIdentifier;
	}

	public Date getCreatedAt() {
		ensureDetailed(createdAt);
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		ensureDetailed(updatedAt);
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getOwned() {
		ensureDetailed(owned);
		return owned;
	}

	public void setOwned(Boolean owned) {
		this.owned = owned;
	}

	public Boolean getSynced() {
		ensureDetailed(synced);
		return synced;
	}

	public void setSynced(Boolean synced) {
		this.synced = synced;
	}

	public String getAddress() {
		ensureDetailed(address);
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public static class Section extends PlexBaseItem {
		private Integer id;
		private String key;
		private String title;
		private String type;
		@JsonDeserialize(using = BooleanDeserializer.class)
		private Boolean shared;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Boolean getShared() {
			return shared;
		}

		public void setShared(Boolean shared) {
			this.shared = shared;
		}
	}
}
