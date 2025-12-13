package kekolab.javaplex;

import java.util.List;

import org.apache.hc.core5.http.message.BasicNameValuePair;

public class PlexMusicSection extends PlexSection {
	public static final String TYPE_DESCRIPTION = "artist";

	@Override
	public PlexSectionQueryBuilder<PlexArtist> all() {
		return new PlexSectionQueryBuilder<PlexArtist>()
				.withSection(this)
				.withPath("all");
	}

	@Override
	public List<PlexAlbum> recentlyAdded() {
		return new PlexSectionQueryBuilder<PlexAlbum>()
				.withSection(this)
				.withPath("recentlyAdded")
				.execute();
	}

	public PlexSectionQueryBuilder<PlexAlbum> albums() {
		return new PlexSectionQueryBuilder<PlexAlbum>()
				.withSection(this)
				.withPath("all")
				.addParameter(new BasicNameValuePair("type", Integer.toString(PlexAlbum.TYPE_ID)));
	}

	public PlexSectionQueryBuilder<PlexTrack> tracks() {
		return new PlexSectionQueryBuilder<PlexTrack>()
				.withSection(this)
				.withPath("all")
				.addParameter(new BasicNameValuePair("type", Integer.toString(PlexTrack.TYPE_ID)));
	}

	public PlexCollections<PlexMusicSection> collections() {
		return new PlexCollections<>(this);
	}

	public List<PlexSectionSecondaryDirectory<PlexArtist>> artistGenres() {
		return byFeature("genre", PlexArtist.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumGenres() {
		return byFeature("genre", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexArtist>> artistMoods() {
		return byFeature("mood", PlexArtist.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumMoods() {
		return byFeature("mood", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexTrack>> trackMoods() {
		return byFeature("mood", PlexTrack.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexArtist>> artistStyles() {
		return byFeature("style", PlexArtist.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumStyles() {
		return byFeature("style", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexArtist>> artistCountries() {
		return byFeature("country", PlexArtist.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexArtist>> artistCollections() {
		return byFeature("collection", PlexArtist.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumCollections() {
		return byFeature("collection", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumYears() {
		return byFeature("year", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumDecades() {
		return byFeature("decade", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumStudios() {
		return byFeature("studio", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumFormats() {
		return byFeature("format", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumSubformats() {
		return byFeature("subformat", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumSources() {
		return byFeature("source", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexAlbum>> albumLabels() {
		return byFeature("label", PlexAlbum.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexTrack>> trackUserRatings() {
		return byFeature("userRating", PlexTrack.TYPE_ID);
	}
}