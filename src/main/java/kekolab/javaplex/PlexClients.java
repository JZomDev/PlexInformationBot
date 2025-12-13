package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

public class PlexClients {
    public static enum MediaType {
        MUSIC, PHOTO, VIDEO;

        @Override
        public String toString() {
            switch (this) {
                case MUSIC:
                    return "music";
                case PHOTO:
                    return "photo";
                case VIDEO:
                    return "video";
            }
            throw new PlexException(); // unreachable
        }
    }

    private final PlexMediaServer server;
    private int commandId;

    protected PlexClients(PlexMediaServer server) {
        this.server = server;
        this.commandId = 0;
    }

    public List<PlexClient> list() {
        try {
            URI uri = new URIBuilder(server.getUri()).appendPath("clients").build();
            PlexGeneralPurposeMediaContainer<?, ?> mc = new PlexGeneralPurposeMediaContainer<>(uri, server);
            return server.getClient().get(server.getToken(), mc).getClients();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private int nextCommandID() {
        return ++commandId;
    }

    // Navigation
    public void navigateBack(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "back"));
    }

    public void contextMenu(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "contextMenu"));
    }

    public void navigateHome(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "home"));
    }

    public void navigateDown(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "moveDown"));
    }

    public void navigateLeft(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "moveLeft"));
    }

    public void navigateRight(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "moveRight"));
    }

    public void navigateUp(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "moveUp"));
    }

    public void navigateToMusic(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "music"));
    }

    public void navigateToNextLetter(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "nextLetter"));
    }

    public void navigateToPageDown(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "pageDown"));
    }

    public void navigateToPageUp(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "pageUp"));
    }

    public void navigateToPreviousLetter(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "previousLetter"));
    }

    public void select(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "select"));
    }

    public void toggleOSD(PlexClient client) {
        ensureCapability(client, "navigation");
        sendCommand(client, Arrays.asList("navigation", "toggleOSD"));
    }

    public void playMedia(PlexClient client, PlexMediatag<?> item, Optional<Long> offset) {
        playMedia(client, item.getServer().playQueues().create(item, false), offset);
    }

    // Playback

    public void pause(PlexClient client, Optional<MediaType> type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        type.ifPresent(t -> params.put("type", t.toString()));
        sendCommand(client, Arrays.asList("playback", "pause"), params);
    }

    public void play(PlexClient client, Optional<MediaType> type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        type.ifPresent(t -> params.put("type", t.toString()));
        sendCommand(client, Arrays.asList("playback", "play"));
    }

    public void playMedia(PlexClient client, PlexPlayQueue queue, Optional<Long> offset) {
        ensureCapability(client, "playback");
        PlexMediaServer pms = queue.server();
        URI pmsUri = pms.getUri();
        List<String> paths = Arrays.asList("playback", "playMedia");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("key", queue.getSelectedItem().getKey());
        offset.ifPresent(o -> parameters.put("offset", Long.toString(o)));
        parameters.put("machineIdentifier", pms.getMachineIdentifier());
        parameters.put("address", pmsUri.getHost());
        parameters.put("port", Integer.toString(pmsUri.getPort()));
        parameters.put("token", pms.createTemporaryToken());
        parameters.put("containerKey", queue.getKey().concat("?window=200&own=1"));
        sendCommand(client, paths, parameters);
    }

    public void refreshPlayQueue(PlexClient client, PlexPlayQueue queue, MediaType type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        params.put("playQueueID", Integer.toString(queue.getPlayQueueID()));
        params.put("type", type.toString());
        sendCommand(client, Arrays.asList("playback", "refreshPlayQueue"), params);
    }

    public void seekTo(PlexClient client, long offset, MediaType type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        params.put("offset", Long.toString(offset));
        params.put("type", type.toString());
        sendCommand(client, Arrays.asList("playback", "seekTo"), params);
    }

    public void skipNext(PlexClient client, Optional<MediaType> type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        type.ifPresent(t -> params.put("type", t.toString()));
        sendCommand(client, Arrays.asList("playback", "skipNext"));
    }

    public void skipPrevious(PlexClient client, Optional<MediaType> type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        type.ifPresent(t -> params.put("type", t.toString()));
        sendCommand(client, Arrays.asList("playback", "skipPrevious"));
    }

    public void skipTo(PlexClient client, PlexMediatag<?> item) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        params.put("key", item.getKey());
        MediaType type;
        if (item instanceof PlexTrack)
            type = MediaType.MUSIC;
        else if (item instanceof PlexVideo)
            type = MediaType.VIDEO;
        else if (item instanceof PlexPhoto)
            type = MediaType.PHOTO;
        else
            throw new PlexException("Unsupported PlexMediatag type");
        params.put("type", type.toString());
        sendCommand(client, Arrays.asList("playback", "skipTo"), params);
    }

    public void stepBack(PlexClient client) {
        ensureCapability(client, "playback");
        sendCommand(client, Arrays.asList("playback", "stepBack"));
    }

    public void stepForward(PlexClient client) {
        ensureCapability(client, "playback");
        sendCommand(client, Arrays.asList("playback", "stepForward"));
    }

    public void stop(PlexClient client, Optional<MediaType> type) {
        ensureCapability(client, "playback");
        Map<String, String> params = new HashMap<>();
        type.ifPresent(t -> params.put("type", t.toString()));
        sendCommand(client, Arrays.asList("playback", "stop"));
    }

    public void setPlaybackParameters(PlexClient client, Optional<Integer> volume, Optional<Boolean> shuffle,
            Optional<PlexRepeatPolicy> repeat, MediaType type) {
        Map<String, String> params = new HashMap<>();
        volume.ifPresent(v -> params.put("volume", Integer.toString(v < 0 ? 0 : v > 100 ? 100 : v)));
        shuffle.ifPresent(s -> params.put("shuffle", s ? "1" : "0"));
        repeat.ifPresent(r -> params.put("repeat", r.toString()));
        if (params.size() == 0)
            return;
        ensureCapability(client, "playback");
        params.put("type", type.toString());
        sendCommand(client, Arrays.asList("playback", "setParameters"), params);
    }

    public void setStreams(PlexClient client, Optional<PlexVideoStream> videoStream,
            Optional<PlexAudioStream> audioStream, Optional<PlexTextStream> textStream, MediaType type) {
        Map<String, String> params = new HashMap<>();
        videoStream.ifPresent(v -> params.put("videoStreamID", Integer.toString(v.getId())));
        audioStream.ifPresent(a -> params.put("audioStreamID", Integer.toString(a.getId())));
        textStream.ifPresent(t -> params.put("subtitleStreamID", Integer.toString(t.getId())));
        if (params.size() == 0)
            return;
        ensureCapability(client, "playback");
        params.put("type", type.toString());
        sendCommand(client, Arrays.asList("playback", "setStreams"), params);
    }

    // Mirroring
    public void showDetails(PlexClient client, PlexMediatag<?> item) {
        ensureCapability(client, "mirror");
        Map<String, String> params = new HashMap<>();
        PlexMediaServer server = item.getServer();
        URI serverUri = server.getUri();
        params.put("key", item.getKey());
        params.put("machineIdentifier", server.getMachineIdentifier());
        params.put("address", serverUri.getHost());
        params.put("port", Integer.toString(serverUri.getPort()));
        params.put("protocol", serverUri.getScheme());
        params.put("token", server.createTemporaryToken());
        sendCommand(client, Arrays.asList("mirror", "details"), params);
    }

    private void sendCommand(PlexClient client, List<String> paths) {
        sendCommand(client, paths, new HashMap<>(0));
    }

    private void sendCommand(PlexClient client, List<String> paths, Map<String, String> parameters) {
        try {
            URIBuilder uri = new URIBuilder()
                    .setScheme(server.getUri().getScheme())
                    .setHost(server.getUri().getHost())
                    .setPort(server.getUri().getPort())
                    .setParameter("X-Plex-Target-Client-Identifier", client.getMachineIdentifier())
                    .appendPath("player");
            paths.stream().forEach(p -> uri.appendPath(p));
            parameters.entrySet().stream().forEach(p -> uri.addParameter(p.getKey(), p.getValue()));
            uri.addParameter("commandID", Integer.toString(nextCommandID()));
            server.getClient().get(uri.build(), server.getToken(), Optional.empty());
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    private void ensureCapability(PlexClient client, String capability) {
        List<String> deviceCapabilities = client.getProtocolCapabilities().stream().map(p -> p.toLowerCase()).toList();
        if (!deviceCapabilities.contains(capability.toLowerCase()))
            throw new PlexException("The target device is required to support the capability `" + capability + "`");
    }

}
