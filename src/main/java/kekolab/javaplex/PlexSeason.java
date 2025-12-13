package kekolab.javaplex;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlexSeason extends PlexMediatag<PlexShowSection> implements PlexChild<PlexShowSection, PlexShow>, PlexParent<PlexShowSection, PlexEpisode> {
	public static final int TYPE_ID = 3;
	public static final String TYPE_DESCRIPTION = "season";

	private Integer leafCount;
	private Integer viewedLeafCount;
	private Integer year;

	@JsonIgnore
	private ChildFeature<PlexShowSection, PlexShow> childFeature;

	public PlexSeason() {
		childFeature = new ChildFeature<>(this);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexSeason s = (PlexSeason) source;
		setLeafCount(s.getLeafCount());
		setViewedLeafCount(s.getViewedLeafCount());
		setYear(s.getYear());
		childFeature.update(s);
	}

	public Integer getLeafCount() {
		return leafCount;
	}

	public Integer getViewedLeafCount() {
		return viewedLeafCount;
	}

	public void setViewedLeafCount(Integer viewedLeafCount) {
		this.viewedLeafCount = viewedLeafCount;
	}

	public void setLeafCount(Integer leafCount) {
		this.leafCount = leafCount;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public List<PlexEpisode> children() {
		return new PlexGeneralPurposeMediaContainer<PlexEpisode, PlexDirectory>(key(), getServer())
				.getMetadata();
	}

	public PlexShow parent() {
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
