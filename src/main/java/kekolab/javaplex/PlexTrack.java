package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;
import kekolab.javaplex.mappers.StringListDeserializer;

public class PlexTrack extends PlexMediatag<PlexMusicSection> implements PlexGrandchild<PlexMusicSection, PlexAlbum, PlexArtist> {
	public static final int TYPE_ID = 10;
	public static final String TYPE_DESCRIPTION = "track";

	public static final PlexFilterableString TITLE = () -> "track.title";
	public static final PlexFilterableTag MOOD = () -> "track.mood";
	public static final PlexFilterableInteger PLAYS = () -> "track.viewCount";
	public static final PlexFilterableDate LAST_PLAYED = () -> "track.lastViewedAt";
	public static final PlexFilterableInteger SKIPS = () -> "track.skipCount";
	public static final PlexFilterableDate LAST_SKIPPED = () -> "track.lastSkippedAt";
	public static final PlexFilterableInteger RATING = () -> "track.userRating";
	public static final PlexFilterableDate LAST_RATED = () -> "track.lastSkippedAt";
	public static final PlexFilterableDate DATE_ADDED = () -> "track.addedAt";
	public static final PlexFilterableInteger FILE_SIZE = () -> "track.mediaSize";
	public static final PlexFilterableInteger BITRATE = () -> "track.mediaBitrate";
	public static final PlexFilterableBoolean TRASHED = () -> "track.trash";

	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> createdAtAccuracy;
	private String createdAtTZOffset;
	private Long duration;
	@JsonProperty("Media")
	private List<PlexMedia> media;
	@JsonProperty("Mood")
	private List<PlexTag> moods;
	private Date originallyAvailableAt;
	private String originalTitle;
	private Integer ratingCount;

	@JsonIgnore
	private GrandchildFeature<PlexMusicSection, PlexAlbum, PlexArtist> grandchildFeature;

	public PlexTrack() {
		createdAtAccuracy = new ArrayList<>();
		media = new ArrayList<>();
		moods = new ArrayList<>();
		grandchildFeature = new GrandchildFeature<>(this);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexTrack track = (PlexTrack) source;
		setCreatedAtAccuracy(track.getCreatedAtAccuracy());
		setCreatedAtTZOffset(track.getCreatedAtTZOffset());
		setDuration(track.getDuration());
		setMedia(track.getMedia());
		setMoods(track.getMoods());
		setOriginallyAvailableAt(track.getOriginallyAvailableAt());
		setOriginalTitle(track.getOriginalTitle());
		setRatingCount(track.getRatingCount());
		grandchildFeature.update(track);
	}

	public List<PlexMedia> getMedia() {
		ensureDetailed(media);
		return media;
	}

	public void setMedia(List<PlexMedia> media) {
		this.media = media;
	}

	public Integer getRatingCount() {
		ensureDetailed(ratingCount);
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}

	public List<PlexTag> getMoods() {
		ensureDetailed(moods);
		return moods;
	}

	public void setMoods(List<PlexTag> moods) {
		this.moods = moods;
	}

	public List<String> getCreatedAtAccuracy() {
		ensureDetailed(createdAtAccuracy);
		return createdAtAccuracy;
	}

	public String getCreatedAtTZOffset() {
		ensureDetailed(createdAtTZOffset);
		return createdAtTZOffset;
	}

	public void setCreatedAtAccuracy(List<String> createdAtAccuracy) {
		this.createdAtAccuracy = createdAtAccuracy;
	}

	public void setCreatedAtTZOffset(String createdAtTZOffset) {
		this.createdAtTZOffset = createdAtTZOffset;
	}

	public String getOriginalTitle() {
		ensureDetailed(originalTitle);
		return originalTitle;
	}

	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}

	public Long getDuration() {
		ensureDetailed(duration);
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Date getOriginallyAvailableAt() {
		ensureDetailed(originallyAvailableAt);
		return originallyAvailableAt;
	}

	public void setOriginallyAvailableAt(Date originallyAvailableAt) {
		this.originallyAvailableAt = originallyAvailableAt;
	}

	public URI parentTheme() {
		ensureDetailed(parentTheme());
		return parentTheme();
	}

	public URI parentThumb() {
		ensureDetailed(parentThumb());
		return parentThumb();
	}

	public Boolean getMoodsLocked() {
		return getFieldLocked("mood");
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}

	public PlexAlbum parent() {
		return grandchildFeature.parent();
	}

	public PlexArtist grandparent() {
		return grandchildFeature.grandparent();
	}

	public String getParentGuid() {
		return grandchildFeature.getParentGuid();
	}

	public String getGrandparentArt() {
		return grandchildFeature.getGrandparentArt();
	}

	public Integer getParentIndex() {
		return grandchildFeature.getParentIndex();
	}

	public String getParentKey() {
		return grandchildFeature.getParentKey();
	}

	public URI grandparentArt() {
		return grandchildFeature.grandparentArt();
	}

	public URI parentKey() {
		return grandchildFeature.parentKey();
	}

	public String getGrandparentGuid() {
		return grandchildFeature.getGrandparentGuid();
	}

	public Integer getParentRatingKey() {
		return grandchildFeature.getParentRatingKey();
	}

	public String getGrandparentKey() {
		return grandchildFeature.getGrandparentKey();
	}

	public URI parentRatingKey() {
		return grandchildFeature.parentRatingKey();
	}

	public URI grandparentKey() {
		return grandchildFeature.grandparentKey();
	}

	public String getParentStudio() {
		return grandchildFeature.getParentStudio();
	}

	public Integer getGrandparentRatingKey() {
		return grandchildFeature.getGrandparentRatingKey();
	}

	public String getParentTheme() {
		return grandchildFeature.getParentTheme();
	}

	public URI grandparentRatingKey() {
		return grandchildFeature.grandparentRatingKey();
	}

	public String getGrandparentTheme() {
		return grandchildFeature.getGrandparentTheme();
	}

	public String getParentThumb() {
		return grandchildFeature.getParentThumb();
	}

	public URI grandparentTheme() {
		return grandchildFeature.grandparentTheme();
	}

	public String getParentTitle() {
		return grandchildFeature.getParentTitle();
	}

	public String getGrandparentThumb() {
		return grandchildFeature.getGrandparentThumb();
	}

	public Integer getParentYear() {
		return grandchildFeature.getParentYear();
	}

	public URI grandparentThumb() {
		return grandchildFeature.grandparentThumb();
	}

	public void setParentGuid(String guid) {
		grandchildFeature.setParentGuid(guid);
	}

	public void setParentIndex(Integer index) {
		grandchildFeature.setParentIndex(index);
	}

	public String getGrandparentTitle() {
		return grandchildFeature.getGrandparentTitle();
	}

	public void setParentStudio(String studio) {
		grandchildFeature.setParentStudio(studio);
	}

	public Integer getGrandparentYear() {
		return grandchildFeature.getGrandparentYear();
	}

	public void setParentTitle(String title) {
		grandchildFeature.setParentTitle(title);
	}

	public void setParentYear(Integer year) {
		grandchildFeature.setParentYear(year);
	}

	public void setGrandparentGuid(String grandParentGuid) {
		grandchildFeature.setGrandparentGuid(grandParentGuid);
	}

	public void setParentKey(String key) {
		grandchildFeature.setParentKey(key);
	}

	public void setGrandparentTitle(String grandparentTitle) {
		grandchildFeature.setGrandparentTitle(grandparentTitle);
	}

	public void setParentRatingKey(Integer ratingKey) {
		grandchildFeature.setParentRatingKey(ratingKey);
	}

	public void setGrandparentYear(Integer grandparentYear) {
		grandchildFeature.setGrandparentYear(grandparentYear);
	}

	public void setParentThumb(String thumb) {
		grandchildFeature.setParentThumb(thumb);
	}

	public void setParentTheme(String theme) {
		grandchildFeature.setParentTheme(theme);
	}

	public void setGrandparentArt(String art) {
		grandchildFeature.setGrandparentArt(art);
	}

	public void setGrandparentKey(String key) {
		grandchildFeature.setGrandparentKey(key);
	}

	public void setGrandparentRatingKey(Integer ratingKey) {
		grandchildFeature.setGrandparentRatingKey(ratingKey);
	}

	public void setGrandparentTheme(String theme) {
		grandchildFeature.setGrandparentTheme(theme);
	}

	public void setGrandparentThumb(String thumb) {
		grandchildFeature.setGrandparentThumb(thumb);
	}

	public void editMoods(List<String> value, Optional<Boolean> lock) {
		editList("mood", value, getMoods());
		if (lock.isPresent())
			editField("mood.locked", lock.get() ? "1" : "0");
	}
}
