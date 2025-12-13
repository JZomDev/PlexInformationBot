package kekolab.javaplex;

import java.net.URI;
import java.util.function.Supplier;

public class ChildFeature<S extends PlexSection, P extends PlexMediatag<S>> implements PlexChild<S, P> {

    private UriProvider parentKey, parentRatingKey, parentTheme, parentThumb;
    private String parentGuid;
    private Integer parentIndex;
    private String parentStudio;
    private String parentTitle;
    private Integer parentYear;

    private PlexMediatag<S> mediatag;

    ChildFeature(PlexMediatag<S> mediatag) {
        this.mediatag = mediatag;
        Supplier<URI> serverUriSupplier = () -> mediatag.getServer().getUri();
        parentKey = new UriProvider(serverUriSupplier);
        parentRatingKey = new UriProvider(serverUriSupplier);
        parentTheme = new UriProvider(serverUriSupplier);
        parentThumb = new UriProvider(serverUriSupplier);
    }

    void update(PlexChild<S, P> source) {
        setParentGuid(source.getParentGuid());
        setParentIndex(source.getParentIndex());
        setParentKey(source.getParentKey());
        setParentRatingKey(source.getParentRatingKey());
        setParentStudio(source.getParentStudio());
        setParentTheme(source.getParentTheme());
        setParentThumb(source.getParentThumb());
        setParentTitle(source.getParentTitle());
        setParentYear(source.getParentYear());
    }

    protected PlexMediatag<S> getMediatag() {
        return mediatag;
    }

    @Override
    public P parent() {
        URI uri = parentKey() != null ? parentKey() : parentRatingKey() != null ? parentRatingKey() : null;
        if (uri != null)
            return new PlexGeneralPurposeMediaContainer<P, PlexDirectory>(uri, mediatag.getServer()).getMetadata().get(0);
        return null;
    }

    @Override
    public String getParentGuid() {
        mediatag.ensureDetailed(parentGuid);
        return parentGuid;
    }

    @Override
    public Integer getParentIndex() {
        mediatag.ensureDetailed(parentIndex);
        return parentIndex;
    }

    @Override
    public String getParentKey() {
        mediatag.ensureDetailed(parentKey.getValue());
        return (String) parentKey.getValue();
    }

    @Override
    public URI parentKey() {
        mediatag.ensureDetailed(parentKey.getValue());
        return parentKey.uri();
    }

    @Override
    public Integer getParentRatingKey() {
        mediatag.ensureDetailed(parentRatingKey.getValue());
        return (Integer) parentRatingKey.getValue();
    }

    @Override
    public URI parentRatingKey() {
        mediatag.ensureDetailed(parentRatingKey.getValue());
        return parentRatingKey.uri();
    }

    @Override
    public String getParentStudio() {
        mediatag.ensureDetailed(parentStudio);
        return parentStudio;
    }

    @Override
    public String getParentTheme() {
        mediatag.ensureDetailed(parentTheme.getValue());
        return (String) parentTheme.getValue();
    }

    @Override
    public URI parentTheme() {
        mediatag.ensureDetailed(parentTheme.getValue());
        return parentTheme.uri();
    }

    @Override
    public String getParentThumb() {
        mediatag.ensureDetailed(parentThumb.getValue());
        return (String) parentThumb.getValue();
    }

    @Override
    public URI parentThumb() {
        mediatag.ensureDetailed(parentThumb.getValue());
        return parentThumb.uri();
    }

    @Override
    public String getParentTitle() {
        mediatag.ensureDetailed(parentTitle);
        return parentTitle;
    }

    @Override
    public Integer getParentYear() {
        mediatag.ensureDetailed(parentYear);
        return parentYear;
    }

    public void setParentGuid(String guid) {
        this.parentGuid = guid;
    }

    public void setParentIndex(Integer index) {
        this.parentIndex = index;
    }

    public void setParentStudio(String studio) {
        this.parentStudio = studio;
    }

    public void setParentTitle(String title) {
        this.parentTitle = title;
    }

    public void setParentYear(Integer year) {
        this.parentYear = year;
    }

    public void setParentKey(String key) {
        this.parentKey.setValue(key);
    }

    public void setParentRatingKey(Integer ratingKey) {
        this.parentRatingKey.setValue(ratingKey);
    }

    public void setParentThumb(String thumb) {
        this.parentThumb.setValue(thumb);
    }

    public void setParentTheme(String theme) {
        this.parentTheme.setValue(theme);
    }

}
