package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.StringListDeserializer;

public class PlexClip extends PlexVideo<PlexPhotoSection> implements PlexChild<PlexPhotoSection, PlexPhotoalbum> {
	public static final int TYPE_ID = 14;
	public static final String TYPE_DESCRIPTION = "clip";

	private Integer createdAtTZOffset;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> createdAtAccuracy;
	private String subtype;
	private String tagline;

	@JsonIgnore
	private ChildFeature<PlexPhotoSection, PlexPhotoalbum> childFeature;

	public PlexClip() {
		createdAtAccuracy = new ArrayList<>();
		childFeature = new ChildFeature<>(this);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexClip clip = (PlexClip) source;
		setCreatedAtTZOffset(clip.getCreatedAtTZOffset());
		setCreatedAtAccuracy(clip.getCreatedAtAccuracy());
		setSubtype(clip.getSubtype());
		setTagline(clip.getTagline());
		childFeature.update(clip);
	}

	public void setParentGuid(String guid) {
		childFeature.setParentGuid(guid);
	}

	public void setParentIndex(Integer index) {
		childFeature.setParentIndex(index);
	}

	public void setParentStudio(String studio) {
		childFeature.setParentStudio(studio);
	}

	public void setParentTitle(String title) {
		childFeature.setParentTitle(title);
	}

	public void setParentYear(Integer year) {
		childFeature.setParentYear(year);
	}

	public void setParentKey(String key) {
		childFeature.setParentKey(key);
	}

	public void setParentRatingKey(Integer ratingKey) {
		childFeature.setParentRatingKey(ratingKey);
	}

	public void setParentThumb(String thumb) {
		childFeature.setParentThumb(thumb);
	}

	public void setParentTheme(String theme) {
		childFeature.setParentTheme(theme);
	}

	public PlexPhotoalbum parent() {
		return childFeature.parent();
	}

	public String getParentGuid() {
		return childFeature.getParentGuid();
	}

	public Integer getParentIndex() {
		return childFeature.getParentIndex();
	}

	public String getParentKey() {
		return childFeature.getParentKey();
	}

	public URI parentKey() {
		return childFeature.parentKey();
	}

	public Integer getParentRatingKey() {
		return childFeature.getParentRatingKey();
	}

	public URI parentRatingKey() {
		return childFeature.parentRatingKey();
	}

	public String getParentStudio() {
		return childFeature.getParentStudio();
	}

	public String getParentTheme() {
		return childFeature.getParentTheme();
	}

	public URI parentTheme() {
		return childFeature.parentTheme();
	}

	public String getParentThumb() {
		return childFeature.getParentThumb();
	}

	public URI parentThumb() {
		return childFeature.parentThumb();
	}

	public String getParentTitle() {
		return childFeature.getParentTitle();
	}

	public Integer getParentYear() {
		return childFeature.getParentYear();
	}

	public Integer getCreatedAtTZOffset() {
		ensureDetailed(createdAtTZOffset);
		return createdAtTZOffset;
	}

	public void setCreatedAtTZOffset(Integer createdAtTZOffset) {
		this.createdAtTZOffset = createdAtTZOffset;
	}

	public List<String> getCreatedAtAccuracy() {
		ensureDetailed(createdAtAccuracy);
		return createdAtAccuracy;
	}

	public void setCreatedAtAccuracy(List<String> createdAtAccuracy) {
		this.createdAtAccuracy = createdAtAccuracy;
	}

	public String getSubtype() {
		ensureDetailed(subtype);
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getTagline() {
		ensureDetailed(tagline);
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public int typeId() {
		return TYPE_ID;
	}

}
