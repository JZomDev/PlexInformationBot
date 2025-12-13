package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

public class PlexCollections<S extends PlexSection> {
    private final S section;

    protected PlexCollections(S section) {
        this.section = section;
    }

    public <M extends PlexMediatag<S>> PlexCollection<S, M> create(String title, M item) {
        PlexMediaServer server = section.getServer();
        URI uri;
        try {
            uri = new URIBuilder(server.getUri()).appendPathSegments("library", "collections")
                    .addParameter("type", Integer.toString(item.typeId())).addParameter("title", title)
                    .addParameter("uri", GenericCollectionsHelper.uriParameter(section.getServer(), item).toString())
                    .addParameter("sectionId", section.getKey())
                    .build();
        } catch (URISyntaxException e) {
            throw new PlexException("Unknown error. See attached stacktrace", e);
        }
        PlexGeneralPurposeMediaContainer<PlexCollection<S, M>, PlexDirectory> container = new PlexGeneralPurposeMediaContainer<>(uri, server);
        return server.getClient().post(server.getToken(), container).getMetadata().get(0);
    }

    public <M extends PlexMediatag<S>> PlexCollection<S, M> add(PlexCollection<S, M> collection, M mediatag) {
        new GenericCollectionsHelper<>(collection, section.getServer());
        return new GenericCollectionsHelper<>(collection, section.getServer()).add(mediatag);
    }

    public <M extends PlexMediatag<S>> PlexCollection<S, M> remove(PlexCollection<S, M> target, M item) {
        try {
            URI uri = new URIBuilder(target.key()).appendPath(Integer.toString(item.getRatingKey()))
                    .addParameter("excludeAllLeaves", "1").build();
            return new GenericCollectionsHelper<>(target, section.getServer()).remove(uri);
        } catch (URISyntaxException e) {
            throw new PlexException(e);
        }
    }

    public List<PlexCollection<S, ? extends PlexMediatag<S>>> list() {
        try {
            URI uri = new URIBuilder(section.key()).appendPath("collections").build();
            return new PlexGeneralPurposeMediaContainer<PlexCollection<S, ? extends PlexMediatag<S>>, PlexDirectory>(uri, section.getServer())
                    .getMetadata();
        } catch (URISyntaxException e) {
            throw new PlexException("Unknown exception. See attached stacktrace", e);
        }
    }

    public void delete(PlexCollection<S, ? extends PlexMediatag<S>> target) {
        PlexMediaServer server = section.getServer();
        server.getClient().delete(target.ratingKey(), server.getToken(), Optional.empty());
    }
}
