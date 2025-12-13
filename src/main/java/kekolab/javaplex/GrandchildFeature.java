package kekolab.javaplex;

import java.net.URI;
import java.util.function.Supplier;

public class GrandchildFeature<S extends PlexSection, P extends PlexMediatag<S>, GP extends PlexMediatag<S>>
        extends ChildFeature<S, P> implements PlexGrandchild<S, P, GP> {
    private UriProvider grandparentArt, grandparentKey, grandparentRatingKey, grandparentTheme, grandparentThumb;
    private String grandparentGuid;
    private String grandparentTitle;
    private Integer grandparentYear;

    GrandchildFeature(PlexMediatag<S> mediatag) {
        super(mediatag);
        Supplier<URI> serverUriSupplier = () -> mediatag.getServer().getUri();
        grandparentArt = new UriProvider(serverUriSupplier);
        grandparentKey = new UriProvider(serverUriSupplier);
        grandparentRatingKey = new UriProvider(serverUriSupplier);
        grandparentTheme = new UriProvider(serverUriSupplier);
        grandparentThumb = new UriProvider(serverUriSupplier);
    }

    void update(PlexGrandchild<S, P, GP> source) {
        setGrandparentArt(source.getGrandparentArt());
        setGrandparentGuid(source.getGrandparentGuid());
        setGrandparentKey(source.getGrandparentKey());
        setGrandparentRatingKey(source.getGrandparentRatingKey());
        setGrandparentTheme(source.getGrandparentTheme());
        setGrandparentThumb(source.getGrandparentThumb());
        setGrandparentTitle(source.getGrandparentTitle());
        setGrandparentYear(source.getGrandparentYear());
    }

    @Override
    public GP grandparent() {
        URI uri = grandparentKey() != null ? grandparentKey()
                : grandparentRatingKey() != null ? grandparentRatingKey() : null;
        if (uri != null)
            return new PlexGeneralPurposeMediaContainer<GP, PlexDirectory>(uri, getMediatag().getServer()).getMetadata().get(0);
        return null;
    }

    @Override
    public String getGrandparentArt() {
        getMediatag().ensureDetailed(grandparentArt.getValue());
        return (String) grandparentArt.getValue();
    }

    @Override
    public URI grandparentArt() {
        getMediatag().ensureDetailed(grandparentArt.getValue());
        return grandparentArt.uri();
    }

    @Override
    public String getGrandparentGuid() {
        getMediatag().ensureDetailed(grandparentGuid);
        return grandparentGuid;
    }

    @Override
    public String getGrandparentKey() {
        getMediatag().ensureDetailed(grandparentKey.getValue());
        return (String) grandparentKey.getValue();
    }

    @Override
    public URI grandparentKey() {
        getMediatag().ensureDetailed(grandparentKey.getValue());
        return grandparentKey.uri();
    }

    @Override
    public Integer getGrandparentRatingKey() {
        getMediatag().ensureDetailed(grandparentRatingKey.getValue());
        return (Integer) grandparentRatingKey.getValue();
    }

    @Override
    public URI grandparentRatingKey() {
        getMediatag().ensureDetailed(grandparentRatingKey.getValue());
        return grandparentRatingKey.uri();
    }

    @Override
    public String getGrandparentTheme() {
        getMediatag().ensureDetailed(grandparentTheme.getValue());
        return (String) grandparentTheme.getValue();
    }

    @Override
    public URI grandparentTheme() {
        getMediatag().ensureDetailed(grandparentTheme.getValue());
        return grandparentTheme.uri();
    }

    @Override
    public String getGrandparentThumb() {
        getMediatag().ensureDetailed(grandparentThumb.getValue());
        return (String) grandparentThumb.getValue();
    }

    @Override
    public URI grandparentThumb() {
        getMediatag().ensureDetailed(grandparentThumb.getValue());
        return grandparentThumb.uri();
    }

    @Override
    public String getGrandparentTitle() {
        getMediatag().ensureDetailed(grandparentTitle);
        return grandparentTitle;
    }

    @Override
    public Integer getGrandparentYear() {
        getMediatag().ensureDetailed(grandparentYear);
        return grandparentYear;
    }

    public void setGrandparentGuid(String grandParentGuid) {
        this.grandparentGuid = grandParentGuid;
    }

    public void setGrandparentTitle(String grandparentTitle) {
        this.grandparentTitle = grandparentTitle;
    }

    public void setGrandparentYear(Integer grandparentYear) {
        this.grandparentYear = grandparentYear;
    }

    public void setGrandparentArt(String art) {
        this.grandparentArt.setValue(art);
    }

    public void setGrandparentKey(String key) {
        this.grandparentKey.setValue(key);
    }

    public void setGrandparentRatingKey(Integer ratingKey) {
        this.grandparentRatingKey.setValue(ratingKey);
    }

    public void setGrandparentTheme(String theme) {
        this.grandparentTheme.setValue(theme);
    }

    public void setGrandparentThumb(String thumb) {
        this.grandparentThumb.setValue(thumb);
    }
}
