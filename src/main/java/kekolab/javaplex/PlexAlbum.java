package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;

public class PlexAlbum extends PlexMediatag<PlexMusicSection> implements PlexChild<PlexMusicSection, PlexArtist>, PlexParent<PlexMusicSection, PlexTrack> {
	public static final int TYPE_ID = 9;
	public static final String TYPE_DESCRIPTION = "album";

	public static final PlexFilterableString TITLE = () -> "album.title";
	public static final PlexFilterableInteger YEAR = () -> "album.year";
	public static final PlexFilterableInteger DECADE = () -> "album.decade";
	public static final PlexFilterableTag GENRE = () -> "album.genre";
	public static final PlexFilterableInteger PLAYS = () -> "album.viewCount";
	public static final PlexFilterableDate LAST_PLAYED = () -> "album.lastViewedAt";
	public static final PlexFilterableInteger RATING = () -> "album.userRating";
	public static final PlexFilterableInteger CRITIC_RATING = () -> "album.rating";
	public static final PlexFilterableString RECORD_LABEL = () -> "album.studio";
	public static final PlexFilterableTag MOOD = () -> "album.mood";
	public static final PlexFilterableTag STYLE = () -> "album.style";
	public static final PlexFilterableTag FORMAT = () -> "album.format";
	public static final PlexFilterableTag TYPE = () -> "album.subformat";
	public static final PlexFilterableTag COLLECTION = () -> "album.collection";
	public static final PlexFilterableDate DATE_ADDED = () -> "album.addedAt";
	public static final PlexFilterableDate DATE_RELEASED = () -> "album.originallyAvailableAt";
	public static final PlexFilterableBoolean UNMATCHED = () -> "album.unmatched";
	public static final PlexFilterableTag SOURCE = () -> "album.source";
	public static final PlexFilterableTag LABEL = () -> "album.label";

	@JsonProperty("Director")
	private List<PlexTag> directors;
	@JsonProperty("Format")
	private List<PlexTag> formats;
	@JsonProperty("Genre")
	private List<PlexTag> genres;
	private Integer leafCount;
	private Integer loudnessAnalysisVersion;
	@JsonProperty("Mood")
	private List<PlexTag> moods;
	private Date originallyAvailableAt;
	private String studio;
	@JsonProperty("Subformat")
	private List<PlexTag> subformats;
	@JsonProperty("Style")
	private List<PlexTag> styles;
	private Double rating;
	private Integer viewedLeafCount;
	private Integer year;

	@JsonIgnore
	private ChildFeature<PlexMusicSection, PlexArtist> childFeature;

	public PlexAlbum() {
		directors = new ArrayList<>();
		formats = new ArrayList<>();
		genres = new ArrayList<>();
		moods = new ArrayList<>();
		subformats = new ArrayList<>();
		styles = new ArrayList<>();
		childFeature = new ChildFeature<>(this);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexAlbum a = (PlexAlbum) source;
		setDirectors(a.getDirectors());
		setFormats(a.getFormats());
		setGenres(a.getGenres());
		setLeafCount(a.getLeafCount());
		setLoudnessAnalysisVersion(a.getLoudnessAnalysisVersion());
		setMoods(a.getMoods());
		setOriginallyAvailableAt(a.getOriginallyAvailableAt());
		setStudio(a.getStudio());
		setSubformats(a.getSubformats());
		setStyles(a.getStyles());
		setRating(a.getRating());
		setViewedLeafCount(a.getViewedLeafCount());
		setYear(a.getYear());
		childFeature.update(a);
	}

	public Double getRating() {
		ensureDetailed(rating);
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public List<PlexTag> getStyles() {
		ensureDetailed(styles);
		return styles;
	}

	public void setStyles(List<PlexTag> styles) {
		this.styles = styles;
	}

	public List<PlexTag> getFormats() {
		ensureDetailed(formats);
		return formats;
	}

	public void setFormats(List<PlexTag> formats) {
		this.formats = formats;
	}

	public Integer getLoudnessAnalysisVersion() {
		ensureDetailed(loudnessAnalysisVersion);
		return loudnessAnalysisVersion;
	}

	public void setLoudnessAnalysisVersion(Integer loudnessAnalysisVersion) {
		this.loudnessAnalysisVersion = loudnessAnalysisVersion;
	}

	public List<PlexTag> getSubformats() {
		ensureDetailed(subformats);
		return subformats;
	}

	public void setSubformats(List<PlexTag> subformats) {
		this.subformats = subformats;
	}

	public List<PlexTag> getDirectors() {
		ensureDetailed(directors);
		return directors;
	}

	public void setDirectors(List<PlexTag> directors) {
		this.directors = directors;
	}

	public Integer getLeafCount() {
		ensureDetailed(leafCount);
		return leafCount;
	}

	public void setLeafCount(Integer leafCount) {
		this.leafCount = leafCount;
	}

	public List<PlexTag> getMoods() {
		ensureDetailed(moods);
		return moods;
	}

	public void setMoods(List<PlexTag> moods) {
		this.moods = moods;
	}

	public String getStudio() {
		ensureDetailed(studio);
		return studio;
	}

	public void setStudio(String studio) {
		this.studio = studio;
	}

	public Integer getViewedLeafCount() {
		ensureDetailed(viewedLeafCount);
		return viewedLeafCount;
	}

	public void setViewedLeafCount(Integer viewedLeafCount) {
		this.viewedLeafCount = viewedLeafCount;
	}

	public List<PlexTag> getGenres() {
		ensureDetailed(genres);
		return genres;
	}

	public void setGenres(List<PlexTag> genres) {
		this.genres = genres;
	}

	public Date getOriginallyAvailableAt() {
		ensureDetailed(originallyAvailableAt);
		return originallyAvailableAt;
	}

	public void setOriginallyAvailableAt(Date originallyAvailableAt) {
		this.originallyAvailableAt = originallyAvailableAt;
	}

	public Integer getYear() {
		ensureDetailed(year);
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public List<PlexTrack> children() {
		return new PlexGeneralPurposeMediaContainer<PlexTrack, PlexDirectory>(key(), getServer()).getMetadata();
	}

	public Boolean getGenresLocked() {
		return getFieldLocked("genre");
	}

	public Boolean getMoodsLocked() {
		return getFieldLocked("mood");
	}

	public Boolean getStylesLocked() {
		return getFieldLocked("style");
	}

	public int typeId() {
		return TYPE_ID;
	}

	public void editGenres(List<String> value, Optional<Boolean> lock) {
		editList("genre", value, getGenres());
		if (lock.isPresent())
			editField("genre.locked", lock.get() ? "1" : "0");
	}

	public void editMoods(List<String> value, Optional<Boolean> lock) {
		editList("mood", value, getMoods());
		if (lock.isPresent())
			editField("mood.locked", lock.get() ? "1" : "0");
	}

	public void editStyles(List<String> value, Optional<Boolean> lock) {
		editList("style", value, getStyles());
		if (lock.isPresent())
			editField("style.locked", lock.get() ? "1" : "0");
	}

	public PlexArtist parent() {
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
}
