package kekolab.javaplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;
import kekolab.javaplex.mappers.StringListDeserializer;

public class PlexMovie extends PlexVideo<PlexMovieSection> {
	public static final int TYPE_ID = 1;
	public static final String TYPE_DESCRIPTION = "movie";

	public static final PlexFilterableString TITLE = () -> "title";
	public static final PlexFilterableString STUDIO = () -> "studio";
	public static final PlexFilterableTag CONTENT_RATING = () -> "contentRating";
	public static final PlexFilterableInteger YEAR = () -> "year";
	public static final PlexFilterableInteger DECADE = () -> "decade";
	public static final PlexFilterableBoolean UNMATCHED = () -> "unmatched";
	public static final PlexFilterableBoolean DUPLICATED = () -> "duplicated";
	public static final PlexFilterableTag GENRE = () -> "genre";
	public static final PlexFilterableTag COLLECTION = () -> "collection";
	public static final PlexFilterableTag DIRECTOR = () -> "director";
	public static final PlexFilterableTag WRITER = () -> "writer";
	public static final PlexFilterableTag PRODUCER = () -> "producer";
	public static final PlexFilterableTag ACTOR = () -> "actor";
	public static final PlexFilterableTag COUNTRY = () -> "country";
	public static final PlexFilterableDate DATE_ADDED = () -> "addedAt";
	public static final PlexFilterableInteger PLAYS = () -> "viewCount";
	public static final PlexFilterableDate LAST_PLAYED = () -> "lastViewedAt";
	public static final PlexFilterableBoolean UNPLAYED = () -> "unwatched";
	public static final PlexFilterableInteger RESOLUTION = () -> "resolution";
	public static final PlexFilterableBoolean HDR = () -> "hdr";
	public static final PlexFilterableInteger FILE_SIZE = () -> "mediaSize";
	public static final PlexFilterableInteger BITRATE = () -> "mediaBitrate";
	public static final PlexFilterableTag SUBTITLE_LANGUAGE = () -> "subtitleLanguage";
	public static final PlexFilterableTag AUDIO_LANGUAGE = () -> "audioLanguage";
	public static final PlexFilterableBoolean IN_PROGRESS = () -> "inProgress";

	private Double audienceRating;
	private String audienceRatingImage;
	private String chapterSource;
	@JsonProperty("Country")
	private List<PlexTag> countries;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> createdAtAccuracy;
	private Integer createdAtTZOffset;
	@JsonProperty("Director")
	private List<PlexTag> directors;
	@JsonProperty("Genre")
	private List<PlexTag> genres;
	private Integer hasPremiumExtras;
	private Integer hasPremiumPrimaryExtra;
	@JsonProperty("Producer")
	private List<PlexTag> producers;
	private Double rating;
	private String ratingImage;
	@JsonProperty("Rating")
	private List<PlexRating> ratings;
	@JsonProperty("Role")
	private List<PlexRole> roles;
	@JsonProperty("Similar")
	private List<PlexTag> similars;
	private String subtype;
	private String tagline;
	@JsonProperty("Writer")
	private List<PlexTag> writers;

	public PlexMovie() {
		countries = new ArrayList<>();
		directors = new ArrayList<>();
		genres = new ArrayList<>();
		producers = new ArrayList<>();
		ratings = new ArrayList<>();
		roles = new ArrayList<>();
		similars = new ArrayList<>();
		writers = new ArrayList<>();
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexMovie movie = (PlexMovie) source;
		setAudienceRating(movie.getAudienceRating());
		setAudienceRatingImage(movie.getAudienceRatingImage());
		setChapterSource(movie.getChapterSource());
		setCountries(movie.getCountries());
		setCreatedAtAccuracy(movie.getCreatedAtAccuracy());
		setCreatedAtTZOffset(movie.getCreatedAtTZOffset());
		setDirectors(movie.getDirectors());
		setGenres(movie.getGenres());
		setHasPremiumExtras(movie.getHasPremiumExtras());
		setHasPremiumPrimaryExtra(movie.getHasPremiumPrimaryExtra());
		setProducers(movie.getProducers());
		setRating(movie.getRating());
		setRatingImage(movie.getRatingImage());
		setRatings(movie.getRatings());
		setRoles(movie.getRoles());
		setSimilars(movie.getSimilars());
		setSubtype(movie.getSubtype());
		setTagline(movie.getTagline());
		setWriters(movie.getWriters());

	}

	public Double getRating() {
		ensureDetailed(rating);
		return rating;
	}

	public Double getAudienceRating() {
		ensureDetailed(audienceRating);
		return audienceRating;
	}

	public String getTagline() {
		ensureDetailed(tagline);
		return tagline;
	}

	public String getAudienceRatingImage() {
		ensureDetailed(audienceRatingImage);
		return audienceRatingImage;
	}

	public Integer getHasPremiumExtras() {
		ensureDetailed(hasPremiumExtras);
		return hasPremiumExtras;
	}

	public Integer getHasPremiumPrimaryExtra() {
		ensureDetailed(hasPremiumPrimaryExtra);
		return hasPremiumPrimaryExtra;
	}

	public String getRatingImage() {
		ensureDetailed(ratingImage);
		return ratingImage;
	}

	public List<PlexTag> getGenres() {
		ensureDetailed(genres);
		return genres;
	}

	public List<PlexTag> getDirectors() {
		ensureDetailed(directors);
		return directors;
	}

	public List<PlexTag> getWriters() {
		ensureDetailed(writers);
		return writers;
	}

	public List<PlexTag> getProducers() {
		ensureDetailed(producers);
		return producers;
	}

	public List<PlexTag> getCountries() {
		ensureDetailed(countries);
		return countries;
	}

	public List<PlexRole> getRoles() {
		ensureDetailed(roles);
		return roles;
	}

	public List<PlexTag> getSimilars() {
		ensureDetailed(similars);
		return similars;
	}

	public void setSimilars(List<PlexTag> similars) {
		this.similars = similars;
	}

	public void setRoles(List<PlexRole> roles) {
		this.roles = roles;
	}

	public void setCountries(List<PlexTag> countries) {
		this.countries = countries;
	}

	public void setProducers(List<PlexTag> producers) {
		this.producers = producers;
	}

	public void setWriters(List<PlexTag> writers) {
		this.writers = writers;
	}

	public void setDirectors(List<PlexTag> directors) {
		this.directors = directors;
	}

	public void setGenres(List<PlexTag> genres) {
		this.genres = genres;
	}

	public void setRatingImage(String ratingImage) {
		this.ratingImage = ratingImage;
	}

	public void setHasPremiumExtras(Integer hasPremiumExtra) {
		this.hasPremiumExtras = hasPremiumExtra;
	}

	public void setAudienceRatingImage(String audienceRatingImage) {
		this.audienceRatingImage = audienceRatingImage;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public void setAudienceRating(Double audienceRating) {
		this.audienceRating = audienceRating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public void setHasPremiumPrimaryExtra(Integer hasPremiumPrimaryExtra) {
		this.hasPremiumPrimaryExtra = hasPremiumPrimaryExtra;
	}

	public String getChapterSource() {
		ensureDetailed(chapterSource);
		return chapterSource;
	}

	public void setChapterSource(String chapterSource) {
		this.chapterSource = chapterSource;
	}

	public List<PlexRating> getRatings() {
		ensureDetailed(ratings);
		return ratings;
	}

	public void setRatings(List<PlexRating> ratings) {
		this.ratings = ratings;
	}

	public String getSubtype() {
		ensureDetailed(subtype);
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public List<String> getCreatedAtAccuracy() {
		ensureDetailed(createdAtAccuracy);
		return createdAtAccuracy;
	}

	public void setCreatedAtAccuracy(List<String> createdAtAccuracy) {
		this.createdAtAccuracy = createdAtAccuracy;
	}

	public Integer getCreatedAtTZOffset() {
		ensureDetailed(createdAtTZOffset);
		return createdAtTZOffset;
	}

	public void setCreatedAtTZOffset(Integer createdAtTZOffset) {
		this.createdAtTZOffset = createdAtTZOffset;
	}

	public Boolean getCountriesLocked() {
		return getFieldLocked("country");
	}

	public Boolean getDirectorsLocked() {
		return getFieldLocked("director");
	}

	public Boolean getGenresLocked() {
		return getFieldLocked("genre");
	}

	public Boolean getWritersLocked() {
		return getFieldLocked("writer");
	}

	public Boolean getProducersLocked() {
		return getFieldLocked("producer");
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}

	public void editCountries(List<String> value, Optional<Boolean> lock) {
		editList("country", value, getCountries());
		if (lock.isPresent())
			editField("country.locked", lock.get() ? "1" : "0");
	}

	public void editDirectors(List<String> value, Optional<Boolean> lock) {
		editList("director", value, getDirectors());
		if (lock.isPresent())
			editField("director.locked", lock.get() ? "1" : "0");
	}

	public void editGenres(List<String> value, Optional<Boolean> lock) {
		editList("genre", value, getGenres());
		if (lock.isPresent())
			editField("genre.locked", lock.get() ? "1" : "0");
	}

	public void editWriters(List<String> value, Optional<Boolean> lock) {
		editList("writer", value, getWriters());
		if (lock.isPresent())
			editField("writer.locked", lock.get() ? "1" : "0");
	}

	public void editProducers(List<String> value, Optional<Boolean> lock) {
		editList("producer", value, getProducers());
		if (lock.isPresent())
			editField("producer.locked", lock.get() ? "1" : "0");
	}
}
