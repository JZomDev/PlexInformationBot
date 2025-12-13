package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.IntegerListDeserializer;
import kekolab.javaplex.mappers.StreamDeserializer;

public class PlexPart extends PlexMediatagAttribute   {
	@JsonProperty("Stream")
	@JsonDeserialize(contentUsing = StreamDeserializer.class)
	private List<PlexStream> streams;
	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> requiredBandwidths;
	private Integer id;
	private Integer hasThumbnail;
	private Integer orientation;
	private Long duration;
	private Long size;
	private String file;
	private String container;
	private String videoProfile;
	private String audioProfile;
	private Boolean has64bitOffsets;
	private Boolean optimizedForStreaming;
	private Boolean accessible;
	private Boolean exists;
	private Integer deepAnalysisVersion;
	private UriProvider key;
	private Integer height;
	private Integer width;
	private String protocol;
	private String decision;
	private Boolean selected;

	
	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	
	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	
	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public PlexPart() {
		streams = new ArrayList<>();
		key = new UriProvider(() -> getParentTag().getServer().getUri());
	}

	
	public Integer getOrientation() {
		ensureDetailed(orientation);
		return orientation;
	}

	public void setOrientation(Integer orientation) {
		this.orientation = orientation;
	}

	
	public Integer getId() {
		ensureDetailed(id);
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	
	public Long getDuration() {
		ensureDetailed(duration);
		return duration;
	}

	
	public String getFile() {
		ensureDetailed(file);
		return file;
	}

	
	public String getContainer() {
		ensureDetailed(container);
		return container;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	
	public String getVideoProfile() {
		ensureDetailed(videoProfile);
		return videoProfile;
	}

	
	public Long getSize() {
		ensureDetailed(size);
		return size;
	}

	public void setVideoProfile(String videoProfile) {
		this.videoProfile = videoProfile;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	
	public String getAudioProfile() {
		ensureDetailed(audioProfile);
		return audioProfile;
	}

	
	public Boolean getHas64bitOffsets() {
		ensureDetailed(has64bitOffsets);
		return has64bitOffsets;
	}

	
	public Boolean getOptimizedForStreaming() {
		ensureDetailed(optimizedForStreaming);
		return optimizedForStreaming;
	}

	public void setAudioProfile(String audioProfile) {
		this.audioProfile = audioProfile;
	}

	public void setHas64bitOffsets(Boolean has64bitOffsets) {
		this.has64bitOffsets = has64bitOffsets;
	}

	public void setOptimizedForStreaming(Boolean optimizedForStreaming) {
		this.optimizedForStreaming = optimizedForStreaming;
	}

	
	public Integer getHasThumbnail() {
		ensureDetailed(hasThumbnail);
		return hasThumbnail;
	}

	public void setHasThumbnail(Integer hasThumbnail) {
		this.hasThumbnail = hasThumbnail;
	}

	
	public List<PlexStream> getStreams() {
		ensureDetailed(streams);
		return streams;
	}

	public void setStreams(List<PlexStream> streams) {
		this.streams = streams;
	}

	
	public Boolean getAccessible() {
		ensureDetailed(accessible);
		return accessible;
	}

	public void setAccessible(Boolean accessible) {
		this.accessible = accessible;
	}

	
	public Boolean getExists() {
		ensureDetailed(exists);
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}

	
	public Integer getDeepAnalysisVersion() {
		ensureDetailed(deepAnalysisVersion);
		return deepAnalysisVersion;
	}

	public void setDeepAnalysisVersion(Integer deepAnalysisVersion) {
		this.deepAnalysisVersion = deepAnalysisVersion;
	}

	
	public List<Integer> getRequiredBandwidths() {
		ensureDetailed(requiredBandwidths);
		return requiredBandwidths;
	}

	public void setRequiredBandwidths(List<Integer> requiredBandwidths) {
		this.requiredBandwidths = requiredBandwidths;
	}

	
	public String getKey() {
		return (String) key.getValue();
	}

	public void setKey(String key) {
		this.key.setValue(key);
	}

	
	public URI key() {
		return key.uri();
	}
}
