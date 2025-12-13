package kekolab.javaplex;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlexTextStream extends PlexStream {
	public static final int TYPE_ID = 3;

	private Boolean selected;
	private Boolean hearingImpaired;
	@JsonProperty("transient")
	private Integer _transient;
	private String format;
	private String providerTitle;
	private String sourceKey;
	private Integer subIndex;
	private Integer userID;
	private Double score;

	public Boolean getSelected() {
		ensureDetailed(selected);
		return selected;
	}

	public Integer getTransient() {
		ensureDetailed(_transient);
		return _transient;
	}

	public String getFormat() {
		ensureDetailed(format);
		return format;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public void setTransient(Integer _transient) {
		this._transient = _transient;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Integer getSubIndex() {
		ensureDetailed(subIndex);
		return subIndex;
	}

	public void setSubIndex(Integer subIndex) {
		this.subIndex = subIndex;
	}

	public Boolean getHearingImpaired() {
		ensureDetailed(hearingImpaired);
		return hearingImpaired;
	}

	public void setHearingImpaired(Boolean hearingImpaired) {
		this.hearingImpaired = hearingImpaired;
	}

	public String getProviderTitle() {
		ensureDetailed(providerTitle);
		return providerTitle;
	}

	public void setProviderTitle(String providerTitle) {
		this.providerTitle = providerTitle;
	}

	public String getSourceKey() {
		ensureDetailed(sourceKey);
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public Integer getUserID() {
		ensureDetailed(userID);
		return userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Double getScore() {
		ensureDetailed(score);
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}
}
