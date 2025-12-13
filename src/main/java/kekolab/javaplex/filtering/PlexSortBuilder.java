package kekolab.javaplex.filtering;

public class PlexSortBuilder {
    public static final String RANDOM = "random";

    public static final String BY_MOVIE_TITLE = "movie.titleSort";
    public static final String BY_MOVIE_RELEASE_DATE = "movie.originallyAvailableAt";
    public static final String BY_MOVIE_CRITIC_RATING = "movie.rating";
    public static final String BY_MOVIE_AUDIENCE_RATING = "movie.audienceRating";
    public static final String BY_MOVIE_DURATION = "movie.duration";
    public static final String BY_MOVIE_DATE_ADDED = "movie.addedAt";
    public static final String BY_MOVIE_DATE_VIEWED = "movie.lastViewedAt";
    public static final String BY_MOVIE_RESOLUTION = "movie.mediaHeight";

    public static final String BY_ARTIST_TITLE = "artist.titleSort";
    public static final String BY_ARTIST_RATING = "artist.userRating";
    public static final String BY_ARTIST_DATE_ADDED = "artist.addedAt";
    public static final String BY_ARTIST_DATE_PLAYED = "artist.lastViewedAt";
    public static final String BY_ARTIST_PLAYS = "artist.viewCount";

    public static final String BY_ALBUM_RATING = "album.userRating";
    public static final String BY_ALBUM_DATE_ADDED = "album.addedAt";
    public static final String BY_ALBUM_DATE_PLAYED = "album.lastViewedAt";
    public static final String BY_ALBUM_PLAYS = "album.viewCount";

    public static final String BY_TRACK_RATING = "track.userRating";
    public static final String BY_TRACK_PLAYS = "track.viewCount";
    public static final String BY_TRACK_DATE_ADDED = "track.addedAt";
    public static final String BY_TRACK_DATE_PLAYED = "track.lastViewedAt";
    public static final String BY_TRACK_POPULARITY = "track.ratingCount";
    public static final String BY_TRACK_BITRATE = "track.mediaBitrate";

    public static final String BY_PHOTO_DATE_ADDED = "photo.addedAt";
    public static final String BY_PHOTO_DATE_TAKEN = "photo.originallyAvailableAt";
    public static final String BY_PHOTO_NAME = "photo.titleSort";

    public static final String BY_SHOW_TITLE = "show.titleSort";
    public static final String BY_SHOW_RELEASE_DATE = "show.originallyAvailableAt";
    public static final String BY_SHOW_CRITIC_RATING = "show.rating";
    public static final String BY_SHOW_AUDIENCE_RATING = "show.audienceRating";
    public static final String BY_SHOW_UNPLAYED = "show.unviewedLeafCount";
    public static final String BY_SHOW_DATE_VIEWED = "show.lastViewedAt";

    public static final String BY_SEASON_SHOW = "season.index";

    public static final String BY_EPISODE_RELEASE_DATE = "episode.originallyAvailableAt";
    public static final String BY_EPISODE_CRITIC_RATING = "episode.rating";
    public static final String BY_EPISODE_AUDIENCE_RATING = "episode.audienceRating";
    public static final String BY_EPISODE_DATE_VIEWED = "episode.lastViewedAt";
    public static final String BY_EPISODE_DATE_ADDED = "episode.addedAt";

    private String by;
    private PlexSortDirection direction;

    public PlexSortBuilder(String by) {
        this.by = by;
    }

    public PlexSortBuilder thenBy(String by) {
        this.by = String.join(",", this.by, by);
        return this;
    }

    public PlexSortBuilder withDirection(PlexSortDirection direction) {
        this.direction = direction;
        return this;
    }

    public String build() {
        String ret = this.by;
        if (this.direction != null)
            ret = ret.concat(":").concat(direction.toString());
        return ret;
    }
}