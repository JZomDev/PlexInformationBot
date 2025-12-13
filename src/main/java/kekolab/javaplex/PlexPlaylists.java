package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

import kekolab.javaplex.filtering.PlexFilter;

public class PlexPlaylists {
    private final PlexMediaServer server;

    protected PlexPlaylists(PlexMediaServer server) {
        this.server = server;
    }

    public List<PlexPlaylist<?>> list() {
        URI uri;
        try {
            uri = new URIBuilder(server.getUri()).appendPath("playlists").build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
        return new PlexGeneralPurposeMediaContainer<PlexPlaylist<?>, PlexDirectory>(uri, server).getMetadata();
    }

    public void delete(PlexPlaylist<?> target) {
        server.getClient().delete(target.ratingKey(), server.getToken(), Optional.empty());
    }

    // Classic playlists
    // Audio playlists
    public PlexClassicPlaylist<PlexTrack> create(String title, PlexTrack track) {
        return create(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, track);
    }

    public PlexClassicPlaylist<PlexTrack> create(String title, PlexAlbum album) {
        return create(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, album);
    }

    public PlexClassicPlaylist<PlexTrack> create(String title, PlexArtist artist) {
        return create(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, artist);
    }

    public PlexClassicPlaylist<PlexTrack> create(String title, PlexMusicSection section, PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, section, filter, false));
    }

    public PlexClassicPlaylist<PlexTrack> create(String title, PlexMusicSection section, PlexFilter filter,
            String sort) {
        return create(buildCreationUri(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, section, filter, false, sort));
    }    

    public PlexClassicPlaylist<PlexTrack> add(PlexClassicPlaylist<PlexTrack> playlist, PlexTrack track) {
        return new GenericCollectionsHelper<>(playlist, server).add(track);
    }

    public PlexClassicPlaylist<PlexTrack> add(PlexClassicPlaylist<PlexTrack> playlist, PlexAlbum album) {
        return new GenericCollectionsHelper<>(playlist, server).add(album);
    }

    public PlexClassicPlaylist<PlexTrack> add(PlexClassicPlaylist<PlexTrack> playlist, PlexArtist artist) {
        return new GenericCollectionsHelper<>(playlist, server).add(artist);
    }

    public PlexClassicPlaylist<PlexTrack> remove(PlexClassicPlaylist<PlexTrack> playlist, PlexTrack track) {
        return findPlaylistItemId(playlist, track)
                .map(playlistItemId -> remove(playlist, playlistItemId))
                .map(p -> (PlexClassicPlaylist<PlexTrack>) p)
                .orElse(playlist);
    }

    // Video playlists
    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexVideo<?> video) {
        return create(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, video);
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexShow show) {
        return create(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, show);
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexEpisode episode) {
        return create(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, episode);
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexMovieSection section, PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, false));
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexMovieSection section, PlexFilter filter,
            String sort) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, false, sort));
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexShowSection section, PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, false));
    }

    public PlexClassicPlaylist<PlexVideo<?>> create(String title, PlexShowSection section, PlexFilter filter,
            String sort) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, false, sort));
    }

    public PlexClassicPlaylist<PlexVideo<?>> add(PlexClassicPlaylist<PlexVideo<?>> playlist, PlexVideo<?> video) {
        return new GenericCollectionsHelper<>(playlist, server).add(video);
    }

    public PlexClassicPlaylist<PlexVideo<?>> add(PlexClassicPlaylist<PlexVideo<?>> playlist, PlexShow show) {
        return new GenericCollectionsHelper<>(playlist, server).add(show);
    }

    public PlexClassicPlaylist<PlexVideo<?>> add(PlexClassicPlaylist<PlexVideo<?>> playlist, PlexEpisode episode) {
        return new GenericCollectionsHelper<>(playlist, server).add(episode);
    }

    public PlexClassicPlaylist<PlexVideo<?>> remove(PlexClassicPlaylist<PlexVideo<?>> playlist, PlexVideo<?> video) {
        return findPlaylistItemId(playlist, video)
                .map(playlistItemId -> remove(playlist, playlistItemId)).orElse(playlist);
    }

    // Photo playlists
    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> create(String title, PlexPhotoalbum album) {
        return create(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, album);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> create(String title, PlexPhoto photo) {
        return create(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, photo);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> create(String title, PlexClip clip) {
        return create(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, clip);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> create(String title, PlexPhotoSection section,
            PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, section, filter, false));
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> create(String title, PlexPhotoSection section,
            PlexFilter filter, String sort) {
        return create(buildCreationUri(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, section, filter, false, sort));
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> add(
            PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> playlist, PlexPhotoalbum album) {
        return new GenericCollectionsHelper<>(playlist, server).add(album);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> add(
            PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> playlist, PlexPhoto photo) {
        return new GenericCollectionsHelper<>(playlist, server).add(photo);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> add(
            PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> playlist, PlexClip clip) {
        return new GenericCollectionsHelper<>(playlist, server).add(clip);
    }

    public PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> remove(
            PlexClassicPlaylist<PlexMediatag<PlexPhotoSection>> playlist, PlexMediatag<PlexPhotoSection> item) {
        return findPlaylistItemId(playlist, item).map(playlistItemId -> remove(playlist, playlistItemId))
                .orElse(playlist);
    }

    // Private methods for handling classic playlists
    private <P extends PlexClassicPlaylist<?>> P create(String title, String type, PlexMediatag<?> tag) {
        try {
            URI uri = new URIBuilder(server.getUri()).appendPath("playlists").addParameter("title", title)
                    .addParameter("type", type)
                    .addParameter("uri", GenericCollectionsHelper.uriParameter(server, tag).toString()).build();

            PlexHTTPClient client = server.getClient();
            Optional<String> token = server.getToken();
            PlexGeneralPurposeMediaContainer<P, PlexDirectory> pmmc = new PlexGeneralPurposeMediaContainer<>(uri, server);
            return client.post(token, pmmc).getMetadata().get(0);
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private <M extends PlexClassicPlaylist<?>> M remove(M playlist, int playlistItemId) {
        try {
            URI uri = new URIBuilder(playlist.key()).appendPath(Integer.toString(playlistItemId)).build();
            return new GenericCollectionsHelper<>(playlist, server).remove(uri);
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private Optional<Integer> findPlaylistItemId(PlexClassicPlaylist<?> playlist, PlexMediatag<?> item) {
        return playlist.items().stream().filter(playlistItem -> playlistItem.getRatingKey().equals(item.getRatingKey()))
                .map(PlexMediatag::getPlaylistItemID).findAny();
    }
    // End of classic playlists

    // Smart playlists
    public <S extends PlexSection, M extends PlexMediatag<S>> PlexSmartPlaylist<S, M> alter(
            PlexSmartPlaylist<S, M> playlist, PlexFilter filter) {
        return alter(alteringUri(playlist, filter));
    }

    public <S extends PlexSection, M extends PlexMediatag<S>> PlexSmartPlaylist<S, M> alter(
            PlexSmartPlaylist<S, M> playlist, PlexFilter filter, String sort) {
        return alter(alteringUri(playlist, filter, sort));
    }

    // Smart Audio playlists
    public PlexSmartPlaylist<PlexMusicSection, PlexTrack> createSmart(String title, PlexMusicSection section,
            PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, section, filter, true));
    }

    public PlexSmartPlaylist<PlexMusicSection, PlexTrack> createSmart(String title, PlexMusicSection section,
            PlexFilter filter, String sort) {
        return create(buildCreationUri(title, PlexPlaylist.AUDIO_SUBTYPE_DESCRIPTION, section, filter, true, sort));
    }

    // Smart Video playlists
    public PlexSmartPlaylist<PlexShowSection, PlexEpisode> createSmart(String title, PlexShowSection section,
            PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, true));
    }

    public PlexSmartPlaylist<PlexShowSection, PlexEpisode> createSmart(String title, PlexShowSection section,
            PlexFilter filter, String sort) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, true, sort));
    }

    public PlexSmartPlaylist<PlexMovieSection, PlexMovie> createSmart(String title, PlexMovieSection section,
            PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, true));
    }

    public PlexSmartPlaylist<PlexMovieSection, PlexMovie> createSmart(String title, PlexMovieSection section,
            PlexFilter filter, String sort) {
        return create(buildCreationUri(title, PlexPlaylist.VIDEO_SUBTYPE_DESCRIPTION, section, filter, true, sort));
    }

    // Smart Photo playlists
    public PlexSmartPlaylist<PlexPhotoSection, PlexMediatag<PlexPhotoSection>> createSmart(String title,
            PlexPhotoSection section, PlexFilter filter) {
        return create(buildCreationUri(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, section, filter, true));
    }

    public PlexSmartPlaylist<PlexPhotoSection, PlexMediatag<PlexPhotoSection>> createSmart(String title,
            PlexPhotoSection section, PlexFilter filter, String sort) {
        return create(buildCreationUri(title, PlexPlaylist.PHOTO_SUBTYPE_DESCRIPTION, section, filter, true, sort));
    }

    // Private methods for smart playlists
    private <M extends PlexSmartPlaylist<?, ?>> M alter(URI uri) {
        PlexGeneralPurposeMediaContainer<M, PlexDirectory> mc = new PlexGeneralPurposeMediaContainer<>(uri, server);
        return server.getClient().put(server.getToken(), mc).getMetadata().get(0);
    }

    private URI alteringUri(PlexSmartPlaylist<?, ?> playlist, PlexFilter filter) {
        try {
            return new URIBuilder(playlist.key()).setParameter("uri",
                    GenericCollectionsHelper.uriParameter(server, playlist.section(), filter).toString()).build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private URI alteringUri(PlexSmartPlaylist<?, ?> playlist, PlexFilter filter, String sort) {
        try {
            return new URIBuilder(playlist.key())
                    .setParameter("uri",
                            GenericCollectionsHelper.uriParameter(server, playlist.section(), filter, sort).toString())
                    .build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }
    // End of smart playlists

    protected PlexMediaServer getServer() {
        return server;
    }

    private URI buildCreationUri(String title, String type, PlexSection section, PlexFilter filter,
            boolean smart) {
        URIBuilder uriBuilder = new URIBuilder(server.getUri()).appendPath("playlists").addParameter("title", title)
                .addParameter("type", type)
                .setParameter("uri", GenericCollectionsHelper.uriParameter(server, section, filter).toString());
        if (smart)
            uriBuilder.addParameter("smart", Integer.toString(1));
        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private URI buildCreationUri(String title, String type, PlexSection section, PlexFilter filter,
            boolean smart, String sort) {
        try {
            return new URIBuilder(buildCreationUri(title, type, section, filter, smart))
                    .setParameter("uri",
                            GenericCollectionsHelper.uriParameter(server, section, filter, sort).toString())
                    .build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private <M extends PlexPlaylist<?>> M create(URI uri) {
        PlexHTTPClient client = server.getClient();
        Optional<String> token = server.getToken();
        PlexGeneralPurposeMediaContainer<M, PlexDirectory> pmmc = new PlexGeneralPurposeMediaContainer<>(uri, server);
        return client.post(token, pmmc).getMetadata().get(0);
    }
}
