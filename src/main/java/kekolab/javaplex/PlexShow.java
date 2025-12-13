package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import kekolab.javaplex.filtering.PlexFilterableBoolean;
import kekolab.javaplex.filtering.PlexFilterableDate;
import kekolab.javaplex.filtering.PlexFilterableInteger;
import kekolab.javaplex.filtering.PlexFilterableString;
import kekolab.javaplex.filtering.PlexFilterableTag;

public class PlexShow extends PlexMediatag<PlexShowSection>
		implements PlexGrandparent<PlexShowSection, PlexSeason, PlexEpisode> {
	public static final int TYPE_ID = 2;
	public static final String TYPE_DESCRIPTION = "show";

	public static PlexFilterableString TITLE = () -> "show.title";
	public static PlexFilterableString STUDIO = () -> "show.title";
	public static PlexFilterableTag NETWORK = () -> "show.network";
	public static PlexFilterableTag COUNTRY = () -> "show.country";
	public static PlexFilterableInteger RATING = () -> "show.userRating";
	public static PlexFilterableTag CONTENT_RATING = () -> "show.contentRating";
	public static PlexFilterableInteger YEAR = () -> "show.year";
	public static PlexFilterableInteger PLAYS = () -> "show.viewCount";
	public static PlexFilterableDate LAST_PLAYED = () -> "show.lastViewedAt";
	public static PlexFilterableTag GENRE = () -> "show.genre";
	public static PlexFilterableTag COLLECTION = () -> "show.collection";
	public static PlexFilterableTag DIRECTOR = () -> "show.director";
	public static PlexFilterableTag WRITER = () -> "show.writer";
	public static PlexFilterableTag PRODUCER = () -> "show.producer";
	public static PlexFilterableTag ACTOR = () -> "show.actor";
	public static PlexFilterableDate DATE_ADDED = () -> "show.addedAt";
	public static PlexFilterableBoolean UNMATCHED = () -> "show.unmatched";
	public static PlexFilterableBoolean UNPLAYED_EPISODES = () -> "show.unwatchedLeaves";
	public static PlexFilterableString LABEL = () -> "show.label";

	private Double audienceRating;
	private String audienceRatingImage;
	private Integer childCount;
	private String contentRating;
	@JsonProperty("Country")
	private List<PlexTag> countries;
	private Long duration;
	@JsonProperty("Genre")
	private List<PlexTag> genres;
	private Integer leafCount;
	@JsonProperty("Location")
	private List<PlexLocation> locations;
	private Date originallyAvailableAt;
	private String originalTitle;
	private Double rating;
	@JsonProperty("Rating")
	private List<PlexRating> ratings;
	@JsonProperty("Role")
	private List<PlexRole> roles;
	@JsonProperty("Similar")
	private List<PlexTag> similars;
	private String studio;
	private String tagline;
	private Integer viewedLeafCount;
	private Integer year;
	private UriProvider banner;
	private UriProvider theme;

	public PlexShow() {
		banner = new UriProvider(this::uri);
		countries = new ArrayList<>();
		genres = new ArrayList<>();
		locations = new ArrayList<>();
		ratings = new ArrayList<>();
		roles = new ArrayList<>();
		similars = new ArrayList<>();
		theme = new UriProvider(this::uri);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexShow s = (PlexShow) source;
		setAudienceRating(s.getAudienceRating());
		setAudienceRatingImage(s.getAudienceRatingImage());
		setChildCount(s.getChildCount());
		setContentRating(s.getContentRating());
		setCountries(s.getCountries());
		setDuration(s.getDuration());
		setGenres(s.getGenres());
		setLeafCount(s.getLeafCount());
		setOriginallyAvailableAt(s.getOriginallyAvailableAt());
		setOriginalTitle(s.getOriginalTitle());
		setRating(s.getRating());
		setRatings(s.getRatings());
		setRoles(s.getRoles());
		setSimilars(s.getSimilars());
		setStudio(s.getStudio());
		setTagline(s.getTagline());
		setViewCount(s.getViewedLeafCount());
		setYear(s.getYear());
		setArt(s.getArt());
		setBanner(s.getBanner());
		setTheme(s.getTheme());
		setThumb(s.getThumb());
	}

	public void setSimilars(List<PlexTag> similars) {
		this.similars = similars;
	}

	public String getStudio() {
		ensureDetailed(studio);
		return studio;
	}

	public String getContentRating() {
		ensureDetailed(contentRating);
		return contentRating;
	}

	public Double getRating() {
		ensureDetailed(rating);
		return rating;
	}

	public Integer getYear() {
		ensureDetailed(year);
		return year;
	}

	public Long getDuration() {
		ensureDetailed(duration);
		return duration;
	}

	public Integer getLeafCount() {
		ensureDetailed(leafCount);
		return leafCount;
	}

	public Integer getViewedLeafCount() {
		ensureDetailed(viewedLeafCount);
		return viewedLeafCount;
	}

	public Integer getChildCount() {
		ensureDetailed(childCount);
		return childCount;
	}

	public List<PlexTag> getGenres() {
		ensureDetailed(genres);
		return genres;
	}

	public void setGenres(List<PlexTag> genres) {
		this.genres = genres;
	}

	public void setChildCount(Integer childCount) {
		this.childCount = childCount;
	}

	public void setViewedLeafCount(Integer viewedLeafCount) {
		this.viewedLeafCount = viewedLeafCount;
	}

	public void setLeafCount(Integer leafCount) {
		this.leafCount = leafCount;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public void setContentRating(String contentRating) {
		this.contentRating = contentRating;
	}

	public void setStudio(String studio) {
		this.studio = studio;
	}

	public void setBanner(String banner) {
		this.banner.setValue(banner);
	}

	public String getBanner() {
		ensureDetailed(banner.getValue());
		return (String) banner.getValue();
	}

	public void setTheme(String theme) {
		this.theme.setValue(theme);
	}

	public String getTheme() {
		ensureDetailed(theme.getValue());
		return (String) theme.getValue();
	}

	public Date getOriginallyAvailableAt() {
		ensureDetailed(originallyAvailableAt);
		return originallyAvailableAt;
	}

	public List<PlexRole> getRoles() {
		ensureDetailed(roles);
		return roles;
	}

	public String getOriginalTitle() {
		ensureDetailed(originalTitle);
		return originalTitle;
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

	public List<PlexTag> getCountries() {
		ensureDetailed(countries);
		return countries;
	}

	public void setRoles(List<PlexRole> roles) {
		this.roles = roles;
	}

	public void setOriginallyAvailableAt(Date originallyAvailableAt) {
		this.originallyAvailableAt = originallyAvailableAt;
	}

	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}

	public void setAudienceRating(Double audienceRating) {
		this.audienceRating = audienceRating;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public void setAudienceRatingImage(String audienceRatingImage) {
		this.audienceRatingImage = audienceRatingImage;
	}

	public void setCountries(List<PlexTag> countries) {
		this.countries = countries;
	}

	public List<PlexLocation> getLocations() {
		ensureDetailed(locations);
		return locations;
	}

	public void setLocations(List<PlexLocation> locations) {
		this.locations = locations;
	}

	public List<PlexTag> getSimilars() {
		ensureDetailed(similars);
		return similars;
	}

	public List<PlexRating> getRatings() {
		ensureDetailed(ratings);
		return ratings;
	}

	public void setRatings(List<PlexRating> ratings) {
		this.ratings = ratings;
	}

	public List<PlexSeason> children() {
		ensureDetailed(key());
		return new PlexGeneralPurposeMediaContainer<PlexSeason, PlexDirectory>(key(), getServer()).getMetadata();
	}

	public List<PlexEpisode> grandchildren() {
		ensureDetailed(ratingKey());
		URI uri;
		try {
			uri = new URIBuilder(ratingKey()).appendPath("allLeaves").build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
		return new PlexGeneralPurposeMediaContainer<PlexEpisode, PlexDirectory>(uri, getServer()).getMetadata();
	}

	public URI banner() {
		ensureDetailed(banner.getValue());
		return banner.uri();
	}

	public URI theme() {
		ensureDetailed(theme.getValue());
		return theme.uri();
	}

	public Boolean getGenresLocked() {
		return getFieldLocked("genre");
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}

	public void editGenres(List<String> value, Optional<Boolean> lock) {
		editList("genre", value, getGenres());
		if (lock.isPresent())
			editField("genre.locked", lock.get() ? "1" : "0");
	}
}
