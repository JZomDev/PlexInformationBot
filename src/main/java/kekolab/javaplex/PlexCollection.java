package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

public class PlexCollection<S extends PlexSection, M extends PlexMediatag<S>> extends PlexSectionItem<S> {
	public static final int TYPE_ID = 18;
	public static final String TYPE_DESCRIPTION = "collection";
	public static final String ALBUM_COLLECTION_SUBTYPE = "album";
	public static final String ARTIST_COLLECTION_SUBTYPE = "artist";
	public static final String EPISODE_COLLECTION_SUBTYPE = "episode";
	public static final String MOVIE_COLLECTION_SUBTYPE = "movie";
	public static final String SEASON_COLLECTION_SUBTYPE = "season";
	public static final String SHOW_COLLECTION_SUBTYPE = "show";
	public static final String TRACK_COLLECTION_SUBTYPE = "track";

	private Integer childCount;
	private String contentRating;
	private Integer index;
	private Integer maxYear;
	private Integer minYear;
	private Integer ratingCount;
	private String subtype;

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexCollection<?, ?> collection = (PlexCollection<?, ?>) source;
		setChildCount(collection.getChildCount());
		setContentRating(collection.getContentRating());
		setIndex(collection.getIndex());
		setMaxYear(collection.getMaxYear());
		setMinYear(collection.getMinYear());
		setRatingCount(collection.getRatingCount());
		setSubtype(collection.getSubtype());
		setArt(collection.getArt());
		setThumb(collection.getThumb());
	}

	public Integer getChildCount() {
		ensureDetailed(childCount);
		return childCount;
	}

	public void setChildCount(Integer childCount) {
		this.childCount = childCount;
	}

	public String getContentRating() {
		ensureDetailed(contentRating);
		return contentRating;
	}

	public void setContentRating(String contentRating) {
		this.contentRating = contentRating;
	}

	public Integer getIndex() {
		ensureDetailed(index);
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getMaxYear() {
		ensureDetailed(maxYear);
		return maxYear;
	}

	public void setMaxYear(Integer maxYear) {
		this.maxYear = maxYear;
	}

	public Integer getMinYear() {
		ensureDetailed(minYear);
		return minYear;
	}

	public void setMinYear(Integer minYear) {
		this.minYear = minYear;
	}

	public Integer getRatingCount() {
		ensureDetailed(ratingCount);
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}

	public String getSubtype() {
		ensureDetailed(subtype);
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public List<M> items() {
		return new PlexGeneralPurposeMediaContainer<M, PlexDirectory>(key(), getServer()).getMetadata();
	}

	@Override
	public URI ratingKey() {
		if (getRatingKey() != null)
			try {
				return new URIBuilder(getServer().getUri())
						.appendPathSegments("library", "collections", Integer.toString(getRatingKey())).build();
			} catch (URISyntaxException e) {
				throw new PlexException(e);
			}
		return null;
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}

	public Boolean getContentRatingLocked() {
		return getFieldLocked("contentRating");
	}

	public void editContentRating(String value, Optional<Boolean> lock) {
		editField("contentRating.value", value);
		if (lock.isPresent())
			editField("contentRating.locked", lock.get() ? "1" : "0");
	}
}
