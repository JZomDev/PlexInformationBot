package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;
import kekolab.javaplex.mappers.StringListDeserializer;

public class PlexPhoto extends PlexMediatag<PlexPhotoSection> implements PlexChild<PlexPhotoSection, PlexPhotoalbum> {
	public static final int TYPE_ID = 13;
	public static final String TYPE_DESCRIPTION = "photo";

	public static final PlexFilterableString TITLE = () -> "title";
	public static final PlexFilterableDate DATE_ADDED = () -> "addedAt";
	public static final PlexFilterableDate DATE_TAKEN = () -> "originallyAvailableAt";
	public static final PlexFilterableInteger RATING = () -> "userRating";
	public static final PlexFilterableInteger FILE_SIZE = () -> "mediaSize";
	public static final PlexFilterableTag TAG = () -> "tag";
	public static final PlexFilterableBoolean TRASHED = () -> "trashed";
	public static final PlexFilterableTag APERTURE = () -> "aperture";
	public static final PlexFilterableTag EXPOSURE = () -> "exposure";
	public static final PlexFilterableTag ISO = () -> "iso";
	public static final PlexFilterableTag LENS = () -> "lens";
	public static final PlexFilterableTag CAMERA_MAKE = () -> "make";
	public static final PlexFilterableTag CAMERA_MODEL = () -> "model";
	public static final PlexFilterableTag DEVICE = () -> "device";

	private Integer createdAtTZOffset;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> createdAtAccuracy;
	@JsonProperty("Media")
	private List<PlexMedia> media;
	private Date originallyAvailableAt;
	private Integer year;

	@JsonIgnore
	private ChildFeature<PlexPhotoSection, PlexPhotoalbum> childFeature;


	public PlexPhoto() {
		createdAtAccuracy = new ArrayList<>();
		media = new ArrayList<>();
		childFeature = new ChildFeature<>(this);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexPhoto p = (PlexPhoto) source;
		setCreatedAtTZOffset(p.getCreatedAtTZOffset());
		setCreatedAtAccuracy(p.getCreatedAtAccuracy());
		setMedia(p.getMedia());
		setOriginallyAvailableAt(p.getOriginallyAvailableAt());
		setYear(p.getYear());
		childFeature.update(p);
	}

	public Integer getCreatedAtTZOffset() {
		ensureDetailed(createdAtTZOffset);
		return createdAtTZOffset;
	}

	public List<String> getCreatedAtAccuracy() {
		ensureDetailed(createdAtAccuracy);
		return createdAtAccuracy;
	}

	public Date getOriginallyAvailableAt() {
		ensureDetailed(originallyAvailableAt);
		return originallyAvailableAt;
	}

	public Integer getYear() {
		ensureDetailed(year);
		return year;
	}

	public List<PlexMedia> getMedia() {
		ensureDetailed(media);
		return media;
	}

	public void setCreatedAtTZOffset(Integer createdAtTZOffset) {
		this.createdAtTZOffset = createdAtTZOffset;
	}

	public void setCreatedAtAccuracy(List<String> createdAtAccuracy) {
		this.createdAtAccuracy = createdAtAccuracy;
	}

	public void setMedia(List<PlexMedia> media) {
		this.media = media;
	}

	public void setOriginallyAvailableAt(Date originallyAvailableAt) {
		this.originallyAvailableAt = originallyAvailableAt;
	}

	public void setYear(Integer year) {
		this.year = year;
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

	@Override
	public int typeId() {
		return TYPE_ID;
	}
}
