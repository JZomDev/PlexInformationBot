package kekolab.javaplex;

public class PlexAudioStream extends PlexStream {
	public static final int TYPE_ID = 2;
	private Boolean selected;
	private Integer channels;
	private Integer samplingRate;
	private Integer bitDepth;
	private String audioChannelLayout;
	private String profile;
	private Double albumGain;
	private Double albumPeak;
	private Double albumRange;
	private Double gain;
	private Double loudness;
	private Double lra;
	private Double peak;
	private String bitrateMode;

	public String getBitrateMode() {
		return bitrateMode;
	}

	public void setBitrateMode(String bitrateMode) {
		this.bitrateMode = bitrateMode;
	}

	public Double getAlbumGain() {
		ensureDetailed(albumGain);
		return albumGain;
	}

	public Double getAlbumPeak() {
		ensureDetailed(albumPeak);
		return albumPeak;
	}

	public Double getAlbumRange() {
		ensureDetailed(albumRange);
		return albumRange;
	}

	public Double getGain() {
		ensureDetailed(gain);
		return gain;
	}

	public Double getLoudness() {
		ensureDetailed(loudness);
		return loudness;
	}

	public Double getLra() {
		ensureDetailed(lra);
		return lra;
	}

	public Double getPeak() {
		ensureDetailed(peak);
		return peak;
	}

	public String getProfile() {
		ensureDetailed(profile);
		return profile;
	}

	public Boolean getSelected() {
		ensureDetailed(selected);
		return selected;
	}

	public Integer getChannels() {
		ensureDetailed(channels);
		return channels;
	}

	public Integer getSamplingRate() {
		ensureDetailed(samplingRate);
		return samplingRate;
	}

	public String getAudioChannelLayout() {
		ensureDetailed(audioChannelLayout);
		return audioChannelLayout;
	}

	public Integer getBitDepth() {
		ensureDetailed(bitDepth);
		return bitDepth;
	}

	public void setAlbumGain(Double albumGain) {
		this.albumGain = albumGain;
	}

	public void setAlbumPeak(Double albumPeak) {
		this.albumPeak = albumPeak;
	}

	public void setAlbumRange(Double albumRange) {
		this.albumRange = albumRange;
	}

	public void setGain(Double gain) {
		this.gain = gain;
	}

	public void setLoudness(Double loudness) {
		this.loudness = loudness;
	}

	public void setLra(Double lra) {
		this.lra = lra;
	}

	public void setPeak(Double peak) {
		this.peak = peak;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public void setChannels(Integer channels) {
		this.channels = channels;
	}

	public void setSamplingRate(Integer samplingRate) {
		this.samplingRate = samplingRate;
	}

	public void setAudioChannelLayout(String audioChannelLayout) {
		this.audioChannelLayout = audioChannelLayout;
	}

	public void setBitDepth(Integer bitDepth) {
		this.bitDepth = bitDepth;
	}
}
