package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.TimestampDeserializer;

public abstract class PlexMetadata extends PlexDirectory {
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date addedAt;
	private String guid;
	@JsonProperty("Field")
	private List<PlexField> fields;
	private Integer ratingKey;
	private String summary;
	private String type;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date updatedAt;
	private Integer viewCount;
	private String titleSort;

	public PlexMetadata() {
		fields = new ArrayList<>();
	}

	public Integer getRatingKey() {
		return ratingKey;
	}

	public void setRatingKey(Integer ratingKey) {
		this.ratingKey = ratingKey;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public List<PlexField> getFields() {
		ensureDetailed(fields);
		return fields;
	}

	public void setFields(List<PlexField> fields) {
		this.fields = fields;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getViewCount() {
		return viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTitleSort() {
		return titleSort;
	}

	public void setTitleSort(String titleSort) {
		this.titleSort = titleSort;
	}

	void update(PlexMetadata source) {
		setAddedAt(source.getAddedAt());
		setFields(source.getFields());
		setGuid(source.getGuid());
		setKey(source.getKey());
		setRatingKey(source.getRatingKey());
		setSummary(source.getSummary());
		setTitle(source.getTitle());
		setType(source.getType());
		setUpdatedAt(source.getUpdatedAt());
		setViewCount(source.getViewCount());
		setTitleSort(source.getTitleSort());
	}

	void refresh() {
		update(new PlexGeneralPurposeMediaContainer<PlexMetadata, PlexDirectory>(ratingKey(), getServer()).getMetadata()
				.get(0));
	}

	void ensureDetailed(Object field) {
		if (!detailed() && (field == null || field instanceof Collection collection && collection.isEmpty()))
			refresh();
	}

	private boolean detailed() {
		return uri().getPath().equalsIgnoreCase(ratingKey().getPath());
	}

	public abstract URI ratingKey();

	protected Boolean getFieldLocked(String field) {
		return getFields().stream().filter(f -> f.getName().equals(field)).map(PlexField::getLocked)
				.findAny().orElse(null);
	}

	protected void editField(String key, String value) {
		try {
			URI uri = new URIBuilder(ratingKey()).addParameter(key, value).build();
			getServer().getClient().put(uri, getServer().getToken(), Optional.empty());
			refresh();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	protected void editList(String key, List<String> value, List<PlexTag> originals) {
		List<String> _originals = originals.stream().map(PlexTag::getTag).toList();
		List<String> removeds = _originals.stream().filter(Predicate.not(value::contains)).toList();
		URIBuilder uri = new URIBuilder(ratingKey());
		if (removeds.size() > 0)
			uri.addParameter(String.format("%s[].tag.tag-", key), String.join(",", removeds));
		for (int i = 0; i < value.size(); i++)
			uri.addParameter(String.format("%s[%d].tag.tag", key, i), value.get(i));
		try {
			getServer().getClient().put(uri.build(), getServer().getToken(), Optional.empty());
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
		refresh();
	}
}