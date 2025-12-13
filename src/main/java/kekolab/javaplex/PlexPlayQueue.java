package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.MediatagDeserializer;

public class PlexPlayQueue extends PlexServerMediaContainer {
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "audio";

    private String identifier;
    private String mediaTagPrefix;
    private Long mediaTagVersion;
    private Integer playQueueID;
    private Integer playQueueSelectedItemID;
    private Long playQueueSelectedItemOffset;
    private Integer playQueueSelectedMetadataItemID;
    private Boolean playQueueShuffled;
    private String playQueueSourceURI;
    private Integer playQueueTotalCount;
    private Integer playQueueVersion;
    private Integer playQueueLastAddedItemID;

    @JsonProperty("Metadata")
    @JsonDeserialize(contentUsing = MediatagDeserializer.class)
    private List<PlexMediatag<?>> items;

    protected PlexPlayQueue(int id, PlexMediaServer server) {
        this(buildUri(id, server), server);
    }

    protected PlexPlayQueue(URI uri, PlexMediaServer server) {
        super(uri, server);
        items = new ArrayList<>();
    }

    private static URI buildUri(int id, PlexMediaServer server) {
        try {
            return new URIBuilder(server.getUri()).appendPath("playQueues").appendPath(Integer.toString(id)).build();
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    public String getIdentifier() {
        ensureFetched(identifier);
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMediaTagPrefix() {
        ensureFetched(mediaTagPrefix);
        return mediaTagPrefix;
    }

    public void setMediaTagPrefix(String mediaTagPrefix) {
        this.mediaTagPrefix = mediaTagPrefix;
    }

    public Long getMediaTagVersion() {
        ensureFetched(mediaTagVersion);
        return mediaTagVersion;
    }

    public void setMediaTagVersion(Long mediaTagVersion) {
        this.mediaTagVersion = mediaTagVersion;
    }

    public Integer getPlayQueueID() {
        ensureFetched(playQueueID);
        return playQueueID;
    }

    public void setPlayQueueID(Integer playQueueID) {
        this.playQueueID = playQueueID;
    }

    public Integer getPlayQueueSelectedItemID() {
        ensureFetched(playQueueSelectedItemID);
        return playQueueSelectedItemID;
    }

    public void setPlayQueueSelectedItemID(Integer playQueueSelectedItemID) {
        this.playQueueSelectedItemID = playQueueSelectedItemID;
    }

    public Long getPlayQueueSelectedItemOffset() {
        ensureFetched(playQueueSelectedItemOffset);
        return playQueueSelectedItemOffset;
    }

    public void setPlayQueueSelectedItemOffset(Long playQueueSelectedItemOffset) {
        this.playQueueSelectedItemOffset = playQueueSelectedItemOffset;
    }

    public Integer getPlayQueueSelectedMetadataItemID() {
        ensureFetched(playQueueSelectedMetadataItemID);
        return playQueueSelectedMetadataItemID;
    }

    public void setPlayQueueSelectedMetadataItemID(Integer playQueueSelectedMetadataItemID) {
        this.playQueueSelectedMetadataItemID = playQueueSelectedMetadataItemID;
    }

    public Boolean getPlayQueueShuffled() {
        ensureFetched(playQueueShuffled);
        return playQueueShuffled;
    }

    public void setPlayQueueShuffled(Boolean playQueueShuffled) {
        this.playQueueShuffled = playQueueShuffled;
    }

    public String getPlayQueueSourceURI() {
        ensureFetched(playQueueSourceURI);
        return playQueueSourceURI;
    }

    public void setPlayQueueSourceURI(String playQueueSourceURI) {
        this.playQueueSourceURI = playQueueSourceURI;
    }

    public Integer getPlayQueueTotalCount() {
        ensureFetched(playQueueTotalCount);
        return playQueueTotalCount;
    }

    public void setPlayQueueTotalCount(Integer playQueueTotalCount) {
        this.playQueueTotalCount = playQueueTotalCount;
    }

    public Integer getPlayQueueVersion() {
        ensureFetched(playQueueVersion);
        return playQueueVersion;
    }

    public void setPlayQueueVersion(Integer playQueueVersion) {
        this.playQueueVersion = playQueueVersion;
    }

    public List<PlexMediatag<?>> getItems() {
        ensureFetched(items);
        items.stream().forEach(i -> i.initialise(server(), getUri()));
        return items;
    }

    public void setItems(List<PlexMediatag<?>> items) {
        this.items = items;
    }

    public Integer getPlayQueueLastAddedItemID() {
        ensureFetched(playQueueLastAddedItemID);
        return playQueueLastAddedItemID;
    }

    public void setPlayQueueLastAddedItemID(Integer playQueueLastAddedItemID) {
        this.playQueueLastAddedItemID = playQueueLastAddedItemID;
    }

    @JsonIgnore
    public String getKey() {
        return "/playQueues/" + getPlayQueueID();
    }

    @JsonIgnore
    public PlexMediatag<?> getSelectedItem() {
        return this.getItems().stream().filter(item -> item.getPlayQueueItemID().equals(this.getPlayQueueSelectedItemID())).findAny().get();
    }
}