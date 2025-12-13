package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;

public class PlexEpisode extends PlexVideo<PlexShowSection>
		implements PlexGrandchild<PlexShowSection, PlexSeason, PlexShow> {
	public static final int TYPE_ID = 4;
	public static final String TYPE_DESCRIPTION = "episode";

	public static final PlexFilterableString TITLE = () -> "episode.title";
	public static final PlexFilterableDate DATE_ADDED = () -> "episode.addedAt";
	public static final PlexFilterableDate AIR_DATE = () -> "episode.originallyAvailableAt";
	public static final PlexFilterableInteger YEAR = () -> "episode.year";
	public static final PlexFilterableInteger RATING = () -> "episode.userRating";
	public static final PlexFilterableInteger PLAYS = () -> "episode.viewCount";
	public static final PlexFilterableDate LAST_PLAYED = () -> "episode.lastViewedAt";
	public static final PlexFilterableBoolean UNWATCHED = () -> "episode.unwatched";
	public static final PlexFilterableBoolean IN_PROGRESS = () -> "episode.inProgress";
	public static final PlexFilterableBoolean DUPLICATED = () -> "episode.duplicate";
	public static final PlexFilterableBoolean HDR = () -> "episode.hdr";
	public static final PlexFilterableTag RESOLUTION = () -> "episode.resolution";
	public static final PlexFilterableInteger FILE_SIZE = () -> "episode.mediaSize";
	public static final PlexFilterableInteger BITRATE = () -> "episode.mediaBitrate";
	public static final PlexFilterableTag SUBTITLE_LANGUAGE = () -> "episode.subtitleLanguage";
	public static final PlexFilterableTag AUDIO_LANGUAGE = () -> "episode.audioLanguage";
	public static final PlexFilterableBoolean TRASHED = () -> "episode.trash";
	public static final PlexFilterableBoolean UNMATCHED = () -> "episode.unmatched";

	private Double rating;
	private String audienceRatingImage;
	@JsonProperty("Director")
	private List<PlexTag> directors;
	@JsonProperty("Role")
	private List<PlexRole> roles;
	@JsonProperty("Writer")
	private List<PlexTag> writers;
	@JsonProperty("Producer")
	private List<PlexTag> producers;
	@JsonProperty("Rating")
	private List<PlexRating> ratings;
	private String chapterSource;
	private Double audienceRating;

	@JsonIgnore
	private GrandchildFeature<PlexShowSection, PlexSeason, PlexShow> grandchildFeature;
	
	public PlexEpisode() {
		directors = new ArrayList<>();
		roles = new ArrayList<>();
		writers = new ArrayList<>();
		producers = new ArrayList<>();
		ratings = new ArrayList<>();
		grandchildFeature = new GrandchildFeature<>(this);
	}


	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexEpisode episode = (PlexEpisode) source;
		setRating(episode.getRating());
		setDirectors(episode.getDirectors());
		setRoles(episode.getRoles());
		setWriters(episode.getWriters());
		setProducers(episode.getProducers());
		setRatings(episode.getRatings());
		setChapterSource(episode.getChapterSource());
		setAudienceRating(episode.getAudienceRating());
		grandchildFeature.update(episode);
	}

	public List<PlexTag> getWriters() {
		ensureDetailed(writers);
		return writers;
	}

	public Double getRating() {
		ensureDetailed(rating);
		return rating;
	}

	public Double getAudienceRating() {
		ensureDetailed(audienceRating);
		return audienceRating;
	}

	public String getAudienceRatingImage() {
		ensureDetailed(audienceRatingImage);
		return audienceRatingImage;
	}

	public List<PlexTag> getDirectors() {
		ensureDetailed(directors);
		return directors;
	}

	public List<PlexRole> getRoles() {
		ensureDetailed(roles);
		return roles;
	}

	public String getChapterSource() {
		ensureDetailed(chapterSource);
		return chapterSource;
	}

	public void setChapterSource(String chapterSource) {
		this.chapterSource = chapterSource;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public void setAudienceRating(Double audienceRating) {
		this.audienceRating = audienceRating;
	}

	public void setAudienceRatingImage(String audienceRatingImage) {
		this.audienceRatingImage = audienceRatingImage;
	}

	public void setDirectors(List<PlexTag> directors) {
		this.directors = directors;
	}

	public void setRoles(List<PlexRole> roles) {
		this.roles = roles;
	}

	public void setWriters(List<PlexTag> writers) {
		this.writers = writers;
	}

	public List<PlexTag> getProducers() {
		ensureDetailed(producers);
		return producers;
	}

	public List<PlexRating> getRatings() {
		ensureDetailed(ratings);
		return ratings;
	}

	public void setRatings(List<PlexRating> ratings) {
		this.ratings = ratings;
	}

	public void setProducers(List<PlexTag> producers) {
		this.producers = producers;
	}

	public Boolean getWritersLocked() {
		return getFieldLocked("writer");
	}

	public Boolean getDirectorsLocked() {
		return getFieldLocked("director");
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}

	public PlexSeason parent() {
		return grandchildFeature.parent();
	}


	public PlexShow grandparent() {
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


	public URI parentTheme() {
		return grandchildFeature.parentTheme();
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


	public URI parentThumb() {
		return grandchildFeature.parentThumb();
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


	public void editWriters(List<String> value, Optional<Boolean> lock) {
		editList("writer", value, getWriters());
		if (lock.isPresent())
			editField("writer.locked", lock.get() ? "1" : "0");
	}

	public void editDirectors(List<String> value, Optional<Boolean> lock) {
		editList("director", value, getDirectors());
		if (lock.isPresent())
			editField("director.locked", lock.get() ? "1" : "0");
	}
}
