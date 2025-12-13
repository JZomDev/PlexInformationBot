package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

public class PlexSectionSecondaryDirectory<T extends PlexMediatag<?>> extends PlexDirectory {
    private UriProvider fastKey;
    private String type;
    private PlexSection section;
    private int mediatagTypeId;

    public PlexSectionSecondaryDirectory() {
        fastKey = new UriProvider(this::uri);
    }

    public String getFastKey() {
        return (String) fastKey.getValue();
    }

    public URI fastKey() {
        return fastKey.uri();
    }

    public String getType() {
        return type;
    }

    public void setFastKey(String fastKey) {
        this.fastKey.setValue(fastKey);
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * This function applies the filter to the following URI:
     * {SECTION.KEY()}/{PATH}?type={TYPE}, i.e., it adds this filter's query
     * parameter to this uri.
     * The queried uri is, though:
     * {SECTION.KEY()}/all?type={TYPE}&{FILTER_QUERY_KEY}={FILTER_QUERY_VALUE}
     * 
     * @param <T>,    the type of the PlexMetadata to return
     * @param section
     * @param path
     * @param type,   the typeId of the PlexMetadata to return
     * @return
     */
    public List<T> list() {
        try {
            String[] keyValue = getConditionQuery().split("=");
            URI uri = new URIBuilder(section.key()).appendPath("all")
                    .addParameter("type", Integer.toString(mediatagTypeId))
                    .addParameter(keyValue[0], keyValue[1])
                    .build();
            return new PlexGeneralPurposeMediaContainer<T, PlexDirectory>(uri, getServer()).getMetadata();
        } catch (URISyntaxException e) {
            throw new PlexException(e); // TODO
        }
    }

    String getConditionQuery() {
        String query = fastKey().getPath();
        return query.substring(query.lastIndexOf("?") + 1);
    }

    protected void initialise(PlexMediaServer server, URI uri, PlexSection section, int mediatagTypeId) {
        initialise(server, uri);
        this.section = section;
        this.mediatagTypeId = mediatagTypeId;
    }
}
