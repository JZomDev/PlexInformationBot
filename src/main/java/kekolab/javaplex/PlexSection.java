package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.TimestampDeserializer;

public abstract class PlexSection extends PlexDirectory {
	private Boolean allowSync;
	private String agent;
	private Boolean content;
	private Long contentChangedAt;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date createdAt;
	private Boolean directory;
	private Integer hidden;
	private String language;
	@JsonProperty("Location")
	private List<PlexLocation> locations = new ArrayList<>();
	private Boolean refreshing;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date scannedAt;
	private String scanner;
	private Boolean filters;
	private String type;
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date updatedAt;
	private String uuid;

	private UriProvider art;
	private UriProvider composite;
	private UriProvider thumb;

	public PlexSection() {
		art = new UriProvider(this::uri);
		composite = new UriProvider(this::uri);
		thumb = new UriProvider(this::uri);
	}

	public void setArt(String art) {
		this.art.setValue(art);
	}

	public String getArt() {
		return (String) art.getValue();
	}

	public URI art() {
		return art.uri();
	}

	public void setComposite(String composite) {
		this.composite.setValue(composite);
	}

	public String getComposite() {
		return (String) composite.getValue();
	}

	public URI composite() {
		return composite.uri();
	}

	public void setThumb(String thumb) {
		this.thumb.setValue(thumb);
	}

	public String getThumb() {
		return (String) thumb.getValue();
	}

	public URI thumb() {
		return thumb.uri();
	}

	public Boolean getAllowSync() {
		return allowSync;
	}

	public Boolean getFilters() {
		return filters;
	}

	public String getType() {
		return type;
	}

	public String getAgent() {
		return agent;
	}

	public String getScanner() {
		return scanner;
	}

	public String getLanguage() {
		return language;
	}

	public String getUuid() {
		return uuid;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getScannedAt() {
		return scannedAt;
	}

	public Boolean getContent() {
		return content;
	}

	public Boolean getDirectory() {
		return directory;
	}

	public Long getContentChangedAt() {
		return contentChangedAt;
	}

	public List<PlexLocation> getLocations() {
		return locations;
	}

	public Boolean getRefreshing() {
		return refreshing;
	}

	public Integer getHidden() {
		return hidden;
	}

	public void setAllowSync(Boolean allowSync) {
		this.allowSync = allowSync;
	}

	public void setFilters(Boolean filters) {
		this.filters = filters;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public void setScanner(String scanner) {
		this.scanner = scanner;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setScannedAt(Date scannedAt) {
		this.scannedAt = scannedAt;
	}

	public void setContent(Boolean content) {
		this.content = content;
	}

	public void setDirectory(Boolean directory) {
		this.directory = directory;
	}

	public void setContentChangedAt(Long contentChangedAt) {
		this.contentChangedAt = contentChangedAt;
	}

	public void setLocations(List<PlexLocation> locations) {
		this.locations = locations;
	}

	public void setRefreshing(Boolean refreshing) {
		this.refreshing = refreshing;
	}

	public void setHidden(Integer hidden) {
		this.hidden = hidden;
	}

	public PlexSectionQueryBuilder<?> all() {
		return new PlexSectionQueryBuilder<>()
				.withSection(this)
				.withPath("all");
	}

	public List<? extends PlexMediatag<?>> recentlyAdded() {
		return new PlexSectionQueryBuilder<>()
				.withSection(this)
				.withPath("recentlyAdded")
				.execute();
	}

	@Override
	public URI key() {
		try {
			return new URIBuilder(getServer().getUri()).appendPathSegments("library", "sections").appendPath(getKey())
					.build();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	protected <D extends PlexSectionSecondaryDirectory<?>> List<D> byFeature(String feature, int type) {
		try {
			URI uri = new URIBuilder(key()).appendPath(feature).build();
			List<D> directories = new PlexGeneralPurposeMediaContainer<PlexMetadata, D>(uri, getServer())
					.getDirectories();
			directories.forEach(d -> d.initialise(getServer(), uri, this, type));
			return directories;
		} catch (URISyntaxException e) {
			throw new PlexException(e); // TODO
		}
	}
	/*
	 * <T> List<T> byFeature(String feature, Function<PlexSectionSecondaryDirectory,
	 * T> function) {
	 * try {
	 * URI uri = new URIBuilder(key()).appendPath(feature).build();
	 * return new MetadataContainer<PlexMetadata,
	 * PlexSectionSecondaryDirectory>(uri, getServer()).getDirectories()
	 * .stream()
	 * .map(function).toList();
	 * } catch (URISyntaxException e) {
	 * throw new PlexException(e); // TODO
	 * }
	 * }
	 */
}