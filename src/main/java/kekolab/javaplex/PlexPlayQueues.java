package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

public class PlexPlayQueues {
    private final PlexMediaServer server;
    private final URI uri;

    protected PlexPlayQueues(PlexMediaServer server) {
        this.server = server;
        try {
            this.uri = new URIBuilder(server.getUri()).appendPath("playQueues").build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    public PlexPlayQueue create(PlexMediatag<?> item, boolean repeat) {
        checkItemType(item);
        String playQueueType;
        String itemType = item.getType();
        if (itemType.equals(PlexArtist.TYPE_DESCRIPTION) || itemType.equals(PlexAlbum.TYPE_DESCRIPTION)
                || itemType.equals(PlexTrack.TYPE_DESCRIPTION))
            playQueueType = PlexPlayQueue.TYPE_AUDIO;
        else
            playQueueType = PlexPlayQueue.TYPE_VIDEO;
        try {
            URI uri = new URIBuilder(this.uri)
                    .addParameter("uri", GenericCollectionsHelper.uriParameter(server, item).toString())
                    .addParameter("type", playQueueType)
                    .addParameter("repeat", repeat ? "1" : "0").build();
            PlexPlayQueue playQueue = new PlexPlayQueue(uri, server);
            server.getClient().post(server.getToken(), playQueue);
            return new PlexPlayQueue(playQueue.getPlayQueueID(), server);
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private void checkItemType(PlexMediatag<?> item) {
        String type = item.getType();
        if (type.equals(PlexPhoto.TYPE_DESCRIPTION) || type.equals(PlexClip.TYPE_DESCRIPTION)
                || type.equals(PlexPhotoalbum.TYPE_DESCRIPTION))
            throw new PlexException("Photos, clips, or photoalbums cannot be added to a play queue");
    }

    public PlexPlayQueue getById(int id) {
        return new PlexPlayQueue(id, server);
    }

    public PlexPlayQueue add(PlexPlayQueue playQueue, PlexMediatag<?> item) {
        checkItemType(item);
        try {
            URI uri = new URIBuilder(playQueue.getUri())
                    .addParameter("uri", GenericCollectionsHelper.uriParameter(server, item).toString()).build();
            server.getClient().put(uri, server.getToken(), Optional.empty());
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
        playQueue.refresh();
        return playQueue;
    }

    public PlexPlayQueue remove(PlexPlayQueue playQueue, PlexMediatag<?> item) {
        Optional<Integer> playQueueItemId = playQueue.getItems().stream()
                .filter(i -> i.getRatingKey().equals(item.getRatingKey())).map(PlexMediatag::getPlayQueueItemID)
                .findAny();
        if (playQueueItemId.isPresent()) {
            try {
                URI uri = new URIBuilder(playQueue.getUri()).appendPath("items")
                        .appendPath(Integer.toString(playQueueItemId.get())).build();
                server.getClient().delete(uri, server.getToken(), Optional.empty());
            } catch (URISyntaxException e) {
                throw new PlexException(e);
            }
            playQueue.refresh();
        }
        return playQueue;
    }

}
