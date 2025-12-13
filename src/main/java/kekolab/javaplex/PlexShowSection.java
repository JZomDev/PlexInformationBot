package kekolab.javaplex;

import java.util.List;

import org.apache.hc.core5.http.message.BasicNameValuePair;

public class PlexShowSection extends PlexSection {
	public static final String TYPE_DESCRIPTION = "show";

	@Override
	public PlexSectionQueryBuilder<PlexShow> all() {
		return new PlexSectionQueryBuilder<PlexShow>()
				.withSection(this)
				.withPath("all");
	}

	@Override
	public List<PlexEpisode> recentlyAdded() {
		return new PlexSectionQueryBuilder<PlexEpisode>()
				.withSection(this)
				.withPath("recentlyAdded")
				.execute();
	}

	public List<PlexEpisode> newest() {
		return new PlexSectionQueryBuilder<PlexEpisode>()
				.withSection(this)
				.withPath("newest")
				.execute();
	}

	public List<PlexEpisode> onDeck() {
		return new PlexSectionQueryBuilder<PlexEpisode>()
				.withSection(this)
				.withPath("onDeck")
				.execute();
	}

	public List<PlexEpisode> recentlyViewed() {
		return new PlexSectionQueryBuilder<PlexEpisode>()
				.withSection(this)
				.withPath("recentlyViewed")
				.execute();
	}

	public List<PlexShow> recentlyViewedShows() {
		return new PlexSectionQueryBuilder<PlexShow>()
				.withSection(this)
				.withPath("recentlyViewedShows")
				.execute();
	}

	public List<PlexShow> unwatched() {
		return new PlexSectionQueryBuilder<PlexShow>()
				.withSection(this)
				.withPath("unwatched")
				.execute();
	}

	public PlexSectionQueryBuilder<PlexSeason> seasons() {
		return new PlexSectionQueryBuilder<PlexSeason>()
				.withSection(this)
				.withPath("all")
				.addParameter(new BasicNameValuePair("type", Integer.toString(PlexSeason.TYPE_ID)));
	}

	public PlexSectionQueryBuilder<PlexEpisode> episodes() {
		return new PlexSectionQueryBuilder<PlexEpisode>()
				.withSection(this)
				.withPath("all")
				.addParameter(new BasicNameValuePair("type", Integer.toString(PlexEpisode.TYPE_ID)));
	}

	public PlexCollections<PlexShowSection> collections() {
		return new PlexCollections<>(this);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showGenres() {
		return byFeature("genre", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showYears() {
		return byFeature("year", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexEpisode>> episodeYears() {
		return byFeature("year", PlexEpisode.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showContentRatings() {
		return byFeature("contentRating", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showStudios() {
		return byFeature("studio", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showNetworks() {
		return byFeature("network", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showCountries() {
		return byFeature("country", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showCollections() {
		return byFeature("collection", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexEpisode>> episodeCollections() {
		return byFeature("collection", PlexEpisode.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showDirectors() {
		return byFeature("director", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showActors() {
		return byFeature("actor", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showWriters() {
		return byFeature("writer", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showProducers() {
		return byFeature("producer", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexShow>> showLabels() {
		return byFeature("label", PlexShow.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexEpisode>> episodeResolutions() {
		return byFeature("resolution", PlexEpisode.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexEpisode>> episodeAudioLanguages() {
		return byFeature("audioLanguage", PlexEpisode.TYPE_ID);
	}

	public List<PlexSectionSecondaryDirectory<PlexEpisode>> episodeSubtitleLanguages() {
		return byFeature("subtitleLanguage", PlexEpisode.TYPE_ID);
	}
}
