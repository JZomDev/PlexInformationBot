package kekolab.javaplex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import kekolab.javaplex.mappers.StringListDeserializer;
import kekolab.javaplex.mappers.TimestampDeserializer;

public class PlexDevice extends PlexBaseItem {
	private String name;
	private String product;
	private String productVersion;
	private String platform;
	private String platformVersion;
	private String device;
	private String clientIdentifier;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date createdAt;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date lastSeenAt;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> provides;
	private Integer owned;
	private String accessToken;
	private String publicAddress;
	private Integer httpsRequired;
	private Integer relay;
	private Integer dnsRebindingProtection;
	private Integer natLoopbackSupported;
	private Integer publicAddressMatches;
	private Integer presence;
	@JsonProperty("Connection")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<PlexConnection> connections;
	private Integer synced;
	@JsonIgnore
	private PlexHTTPClient client;

	public PlexDevice() {
		connections = new ArrayList<>();
		provides = new ArrayList<>();
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getClientIdentifier() {
		return clientIdentifier;
	}

	public void setClientIdentifier(String clientIdentifier) {
		this.clientIdentifier = clientIdentifier;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getLastSeenAt() {
		return lastSeenAt;
	}

	public void setLastSeenAt(Date lastSeenAt) {
		this.lastSeenAt = lastSeenAt;
	}

	public List<String> getProvides() {
		return provides;
	}

	public void setProvides(List<String> provides) {
		this.provides = provides;
	}

	public Integer getOwned() {
		return owned;
	}

	public void setOwned(Integer owned) {
		this.owned = owned;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getPublicAddress() {
		return publicAddress;
	}

	public void setPublicAddress(String publicAddress) {
		this.publicAddress = publicAddress;
	}

	public Integer getHttpsRequired() {
		return httpsRequired;
	}

	public void setHttpsRequired(Integer httpsRequired) {
		this.httpsRequired = httpsRequired;
	}

	public Integer getRelay() {
		return relay;
	}

	public void setRelay(Integer relay) {
		this.relay = relay;
	}

	public Integer getDnsRebindingProtection() {
		return dnsRebindingProtection;
	}

	public void setDnsRebindingProtection(Integer dnsRebindingProtection) {
		this.dnsRebindingProtection = dnsRebindingProtection;
	}

	public Integer getNatLoopbackSupported() {
		return natLoopbackSupported;
	}

	public void setNatLoopbackSupported(Integer natLoopbackSupported) {
		this.natLoopbackSupported = natLoopbackSupported;
	}

	public Integer getPublicAddressMatches() {
		return publicAddressMatches;
	}

	public void setPublicAddressMatches(Integer publicAddressMatches) {
		this.publicAddressMatches = publicAddressMatches;
	}

	public Integer getPresence() {
		return presence;
	}

	public void setPresence(Integer presence) {
		this.presence = presence;
	}

	public List<PlexConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<PlexConnection> connections) {
		this.connections = connections;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSynced() {
		return synced;
	}

	public void setSynced(Integer synced) {
		this.synced = synced;
	}

	protected void setClient(PlexHTTPClient client) {
		this.client = client;
	}

	public boolean isServer() {
		return getProvides().stream().anyMatch(p -> p.equalsIgnoreCase("server"));
	}
}
