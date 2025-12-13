package kekolab.javaplex;

import java.util.List;




public class PlexMovieSection extends PlexSection {
	public static final String TYPE_DESCRIPTION = "movie";   

	@Override
	public PlexSectionQueryBuilder<PlexMovie> all() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("all");
	}

	@Override
	public List<PlexMovie> recentlyAdded() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("recentlyAdded")
				.execute();
	}


	
	public List<PlexMovie> newest() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("newest")
				.execute();
	}

	
	public List<PlexMovie> onDeck() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("onDeck")
				.execute();
	}

	
	public List<PlexMovie> recentlyViewed() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("recentlyViewed")
				.execute();
	}

	
	public List<PlexMovie> unwatched() {
		return new PlexSectionQueryBuilder<PlexMovie>()
				.withSection(this)
				.withPath("unwatched")
				.execute();
	}

	
	public PlexCollections<PlexMovieSection> collections() {
		return new PlexCollections<>(this);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieGenres() {
		return byFeature("genre", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieYears() {
		return byFeature("year", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieDecades() {
		return byFeature("decade", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieContentRatings() {
		return byFeature("contentRating", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieCollections() {
		return byFeature("collection", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieDirectors() {
		return byFeature("director", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieActors() {
		return byFeature("actor", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieWriters() {
		return byFeature("writer", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieProducers() {
		return byFeature("producer", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieCountries() {
		return byFeature("country", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieStudios() {
		return byFeature("studio", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieResolutions() {
		return byFeature("resolution", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieHDRs() {
		return byFeature("hdr", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieAudioLanguages() {
		return byFeature("audioLanguage", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieSubtitleLanguages() {
		return byFeature("subtitleLanguage", PlexMovie.TYPE_ID);
	}

	
	public List<PlexSectionSecondaryDirectory<PlexMovie>> movieLabels() {
		return byFeature("label", PlexMovie.TYPE_ID);
	}
}
