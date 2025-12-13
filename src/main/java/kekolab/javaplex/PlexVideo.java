package kekolab.javaplex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class PlexVideo<S extends PlexSection> extends PlexMediatag<S> {
	private String contentRating;
	private Long duration;
	@JsonProperty("Media")
	private List<PlexMedia> media;
	private Date originallyAvailableAt;
	private String originalTitle;
	private String studio;
	private Integer year;

	public PlexVideo() {
		media = new ArrayList<>();
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexVideo<?> video = (PlexVideo<?>) source;
		setContentRating(video.getContentRating());
		setDuration(video.getDuration());
		setMedia(video.getMedia());
		setOriginallyAvailableAt(video.getOriginallyAvailableAt());
		setOriginalTitle(video.getOriginalTitle());
		setStudio(video.getStudio());
		setYear(video.getYear());
	}

	public String getContentRating() {
		ensureDetailed(contentRating);
		return contentRating;
	}

	public void setContentRating(String contentRating) {
		this.contentRating = contentRating;
	}

	public Long getDuration() {
		ensureDetailed(duration);
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public List<PlexMedia> getMedia() {
		ensureDetailed(media);
		return media;
	}

	public void setMedia(List<PlexMedia> media) {
		this.media = media;
	}

	public Date getOriginallyAvailableAt() {
		ensureDetailed(originallyAvailableAt);
		return originallyAvailableAt;
	}

	public void setOriginallyAvailableAt(Date originallyAvailableAt) {
		this.originallyAvailableAt = originallyAvailableAt;
	}

	public String getOriginalTitle() {
		ensureDetailed(originalTitle);
		return originalTitle;
	}

	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}

	public String getStudio() {
		ensureDetailed(studio);
		return studio;
	}

	public void setStudio(String studio) {
		this.studio = studio;
	}

	public Integer getYear() {
		ensureDetailed(year);
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
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
