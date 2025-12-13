package kekolab.javaplex;

import java.util.List;

public class PlexPhotoSection extends PlexSection {
	public static final String TYPE_DESCRIPTION = "photo";

	private Boolean enableAutoPhotoTags;

	
	public Boolean getEnableAutoPhotoTags() {
		return enableAutoPhotoTags;
	}

	public void setEnableAutoPhotoTags(Boolean enableAutoPhotoTags) {
		this.enableAutoPhotoTags = enableAutoPhotoTags;
	}

	@Override
	public PlexSectionQueryBuilder<PlexPhotoalbum> all() {
		return new PlexSectionQueryBuilder<PlexPhotoalbum>()
				.withSection(this)
				.withPath("all");
	}

	@Override
	public List<PlexPhotoalbum> recentlyAdded() {
		return new PlexSectionQueryBuilder<PlexPhotoalbum>()
				.withSection(this)
				.withPath("recentlyAdded")
				.execute();
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byYear() {
		return byFeature("year", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byCameraMake() {
		return byFeature("make", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byCameraModel() {
		return byFeature("model", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byAperture() {
		return byFeature("aperture", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byShutterSpeed() {
		return byFeature("exposure", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byISO() {
		return byFeature("iso", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byLens() {
		return byFeature("lens", PlexPhoto.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexPhoto>> byTag() {
		return byFeature("tag", PlexPhoto.TYPE_ID);
	}
}
