package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.BooleanDeserializer;

public class PlexAccount extends PlexBaseItem {
	@JsonIgnore
	private static URI uri;
	@JsonIgnore
	private PlexHTTPClient plexClient;
	@JsonIgnore
	private String token;
	@JsonIgnore
	private boolean fetched;

	private String email;
	private Integer id;
	private String uuid;
	@JsonProperty("mailing_list_status")
	private String mailingListStatus;
	private String username;
	private String title;
	private String cloudSyncDevice;
	private String locale;
	private String authenticationToken;
	private String authToken;
	private String scrobbleTypes;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean restricted;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean home;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean guest;
	private String queueEmail;
	private String queueUid;
	private Boolean hasPassword;
	private Integer homeSize;
	private Integer maxHomeSize;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private Boolean secure;
	private Integer certificateVersion;
	@JsonProperty("authentication-token")
	private String authentication_Token;
	private String thumb;

	private static URI buildUri() {
		if (uri != null)
			try {
				uri = new URI("https://plex.tv/users/account");
			} catch (URISyntaxException e) {
				throw new PlexException(e);
			}
		return uri;
	}

	public PlexAccount(PlexHTTPClient client, String token) {
		this.plexClient = client;
		this.token = token;
		this.fetched = false;
	}

	protected void fetch() {
		if (!fetched) {
			fetched = true;
			try {
				plexClient.executeAndUpdateFromResponse(ClassicRequestBuilder.get(buildUri()).build(), this, token);
			} catch (Exception e) {
				fetched = false;
				throw new PlexException(e);
			}
		}
	}

	public String getEmail() {
		fetch();
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getId() {
		fetch();
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		fetch();
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getMailingListStatus() {
		fetch();
		return mailingListStatus;
	}

	public void setMailingListStatus(String mailingListStatus) {
		this.mailingListStatus = mailingListStatus;
	}

	public String getUsername() {
		fetch();
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTitle() {
		fetch();
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCloudSyncDevice() {
		fetch();
		return cloudSyncDevice;
	}

	public void setCloudSyncDevice(String cloudSyncDevice) {
		this.cloudSyncDevice = cloudSyncDevice;
	}

	public String getLocale() {
		fetch();
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getAuthenticationToken() {
		fetch();
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	public String getAuthToken() {
		fetch();
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getScrobbleTypes() {
		fetch();
		return scrobbleTypes;
	}

	public void setScrobbleTypes(String scrobbleTypes) {
		this.scrobbleTypes = scrobbleTypes;
	}

	public Boolean getRestricted() {
		fetch();
		return restricted;
	}

	public void setRestricted(Boolean restricted) {
		this.restricted = restricted;
	}

	public Boolean getHome() {
		fetch();
		return home;
	}

	public void setHome(Boolean home) {
		this.home = home;
	}

	public Boolean getGuest() {
		fetch();
		return guest;
	}

	public void setGuest(Boolean guest) {
		this.guest = guest;
	}

	public String getQueueEmail() {
		fetch();
		return queueEmail;
	}

	public void setQueueEmail(String queueEmail) {
		this.queueEmail = queueEmail;
	}

	public Boolean getHasPassword() {
		fetch();
		return hasPassword;
	}

	public void setHasPassword(Boolean hasPassword) {
		this.hasPassword = hasPassword;
	}

	public Integer getHomeSize() {
		fetch();
		return homeSize;
	}

	public void setHomeSize(Integer homeSize) {
		this.homeSize = homeSize;
	}

	public Integer getMaxHomeSize() {
		fetch();
		return maxHomeSize;
	}

	public void setMaxHomeSize(Integer maxHomeSize) {
		this.maxHomeSize = maxHomeSize;
	}

	public Boolean getSecure() {
		fetch();
		return secure;
	}

	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	public Integer getCertificateVersion() {
		fetch();
		return certificateVersion;
	}

	public void setCertificateVersion(Integer certificateVersion) {
		this.certificateVersion = certificateVersion;
	}

	public String getAuthentication_Token() {
		fetch();
		return authentication_Token;
	}

	public void setAuthentication_Token(String authentication_Token) {
		this.authentication_Token = authentication_Token;
	}

	public String getQueueUid() {
		fetch();
		return queueUid;
	}

	public void setQueueUid(String queueUid) {
		this.queueUid = queueUid;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String getThumb() {
		fetch();
		return thumb;
	}

	public URI thumb() {
		fetch();
		if (thumb != null)
			try {
				return new URI(thumb);
			} catch (URISyntaxException e) {
				throw new PlexException(e);
			}
		return null;
	}
}
