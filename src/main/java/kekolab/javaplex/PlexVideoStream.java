package kekolab.javaplex;

public class PlexVideoStream extends PlexStream {
	public static final int TYPE_ID = 1;
	
	private Integer bitDepth;
	private Integer codedHeight;
	private Integer codedWidth;
	private Integer height;
	private Integer level;
	private Integer refFrames;
	private Integer width;
	private Integer orientation;
	private Boolean hasScalingMatrix;
	private Boolean anamorphic;
	private String chromaLocation;
	private String chromaSubsampling;
	private String colorPrimaries;
	private String colorRange;
	private String colorSpace;
	private String colorTrc;
	private String profile;
	private String scanType;
	private String pixelAspectRatio;
	private String codecID;

	public String getCodecID() {
		ensureDetailed(codecID);
		return codecID;
	}

	public void setCodecID(String codecID) {
		this.codecID = codecID;
	}

	private Double frameRate;

	public String getChromaSubsampling() {
		ensureDetailed(chromaSubsampling);
		return chromaSubsampling;
	}

	public void setChromaSubsampling(String chromaSubsampling) {
		this.chromaSubsampling = chromaSubsampling;
	}

	public Integer getBitDepth() {
		ensureDetailed(bitDepth);
		return bitDepth;
	}

	public Integer getCodedHeight() {
		ensureDetailed(codedHeight);
		return codedHeight;
	}

	public Integer getCodedWidth() {
		ensureDetailed(codedWidth);
		return codedWidth;
	}

	public Boolean getAnamorphic() {
		ensureDetailed(anamorphic);
		return anamorphic;
	}

	public void setAnamorphic(Boolean anamorphic) {
		this.anamorphic = anamorphic;
	}

	public String getPixelAspectRatio() {
		ensureDetailed(pixelAspectRatio);
		return pixelAspectRatio;
	}

	public void setPixelAspectRatio(String pixelAspectRatio) {
		this.pixelAspectRatio = pixelAspectRatio;
	}

	public Integer getHeight() {
		ensureDetailed(height);
		return height;
	}

	public Integer getLevel() {
		ensureDetailed(level);
		return level;
	}

	public Integer getRefFrames() {
		ensureDetailed(refFrames);
		return refFrames;
	}

	public Integer getWidth() {
		ensureDetailed(width);
		return width;
	}

	public Boolean getHasScalingMatrix() {
		ensureDetailed(hasScalingMatrix);
		return hasScalingMatrix;
	}

	public String getChromaLocation() {
		ensureDetailed(chromaLocation);
		return chromaLocation;
	}

	public String getColorPrimaries() {
		ensureDetailed(colorPrimaries);
		return colorPrimaries;
	}

	public String getColorRange() {
		ensureDetailed(colorRange);
		return colorRange;
	}

	public Double getFrameRate() {
		ensureDetailed(frameRate);
		return frameRate;
	}

	public void setBitDepth(Integer bitDepth) {
		this.bitDepth = bitDepth;
	}

	public void setCodedHeight(Integer codedHeight) {
		this.codedHeight = codedHeight;
	}

	public void setCodedWidth(Integer codedWidth) {
		this.codedWidth = codedWidth;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public void setRefFrames(Integer refFrames) {
		this.refFrames = refFrames;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public void setHasScalingMatrix(Boolean hasScalingMatrix) {
		this.hasScalingMatrix = hasScalingMatrix;
	}

	public void setChromaLocation(String chromaLocation) {
		this.chromaLocation = chromaLocation;
	}

	public void setColorPrimaries(String colorPrimaries) {
		this.colorPrimaries = colorPrimaries;
	}

	public void setColorRange(String colorRange) {
		this.colorRange = colorRange;
	}

	public void setFrameRate(Double frameRate) {
		this.frameRate = frameRate;
	}

	public String getColorSpace() {
		ensureDetailed(colorSpace);
		return colorSpace;
	}

	public String getColorTrc() {
		ensureDetailed(colorTrc);
		return colorTrc;
	}

	public String getProfile() {
		ensureDetailed(profile);
		return profile;
	}

	public String getScanType() {
		ensureDetailed(scanType);
		return scanType;
	}

	public void setColorSpace(String colorSpace) {
		this.colorSpace = colorSpace;
	}

	public void setColorTrc(String colorTrc) {
		this.colorTrc = colorTrc;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setScanType(String scanType) {
		this.scanType = scanType;
	}

	public Integer getOrientation() {
		ensureDetailed(orientation);
		return orientation;
	}

	public void setOrientation(Integer orientation) {
		this.orientation = orientation;
	}

}
