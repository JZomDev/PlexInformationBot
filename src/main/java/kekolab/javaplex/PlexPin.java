package kekolab.javaplex;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The PIN is used by Plex to authorize an application to access the public
 * servers of a user
 * 
 * @see PlexAuthenticator
 *
 */
public class PlexPin extends PlexBaseItem {
	@JsonProperty
	private Integer id;
	@JsonProperty
	private String code;
	@JsonProperty(value = "expires-at")
	private Date expiresAt;
	@JsonProperty(value = "client-identifier")
	private String clientIdentifier;
	@JsonProperty
	private Boolean trusted;
	@JsonProperty(value = "user-id")
	private Integer userId;
	@JsonProperty(value = "auth-token")
	private String authToken;
	@JsonProperty(value = "auth_token")
	private String auth_Token;

	/**
	 * 
	 * @return the id of this pin
	 */

	public Integer getId() {
		return id;
	}

	/**
	 * 
	 * @return the code that has to be authorized on
	 *         <code>https://plex.tv/link</code>
	 */

	public String getCode() {
		return code;
	}

	/**
	 * 
	 * @return the expiration date of this pin
	 */

	public Date getExpiresAt() {
		return expiresAt;
	}

	/**
	 * 
	 * @return the client identifier of this pin
	 */

	public String getClientIdentifier() {
		return clientIdentifier;
	}

	/**
	 * 
	 * @return the trusted attribute of this pin
	 */

	public Boolean getTrusted() {
		return trusted;
	}

	/**
	 * 
	 * @return the user id of this pin
	 */

	public Integer getUserId() {
		return userId;
	}

	/**
	 * 
	 * @return the authentication token associated with this pin
	 */

	public String getAuthToken() {
		return authToken;
	}

	/**
	 * 
	 * @return the authentication token associated with this pin
	 */

	public String getAuth_Token() {
		return auth_Token;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	public void setClientIdentifier(String clientIdentifier) {
		this.clientIdentifier = clientIdentifier;
	}

	public void setTrusted(Boolean trusted) {
		this.trusted = trusted;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public void setAuth_Token(String auth_Token) {
		this.auth_Token = auth_Token;
	}
}
