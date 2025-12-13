package kekolab.javaplex;

import java.net.URI;
import java.util.Optional;

public abstract class PlexSectionItem<S extends PlexSection> extends PlexMetadata {
    // TODO Delete a sectionItem? It is deleted just like a PlexCollection (see
    // classes PlexCollections)

    private String librarySectionTitle;
    private Integer librarySectionID;
    private String librarySectionKey;
    private UriProvider art;
	private UriProvider thumb;

    protected PlexSectionItem() {
        this.art = new UriProvider(() -> uri());
        this.thumb = new UriProvider(() -> uri());
    }    

    @Override
    void update(PlexMetadata source) {
        super.update(source);
        PlexSectionItem<?> item = (PlexSectionItem<?>) source;
        setLibrarySectionID(item.getLibrarySectionID());
        setLibrarySectionKey(item.getLibrarySectionKey());
        setLibrarySectionTitle(item.getLibrarySectionTitle());
    }

    public S section() {
        if (librarySectionID != null)
            return (S) getServer().library().sections().byId(librarySectionID);
        return null;
    }

    public String getArt() {
        ensureDetailed(art.getValue());
        return (String) art.getValue();
    }

    
    public void setArt(String art) {
        this.art.setValue(art);
    }

    public URI art() {
        ensureDetailed(art.getValue());
        return art.uri();
    }

    public String getThumb() {
        ensureDetailed(thumb.getValue());
        return (String) thumb.getValue();
    }

    public URI thumb() {
        ensureDetailed(thumb.getValue());
        return thumb.uri();
    }

    public void setThumb(String thumb) {
        this.thumb.setValue(thumb);
    }

    public String getLibrarySectionTitle() {
        ensureDetailed(librarySectionTitle);
        return librarySectionTitle;
    }

    public Integer getLibrarySectionID() {
        ensureDetailed(librarySectionID);
        return librarySectionID;
    }

    public String getLibrarySectionKey() {
        ensureDetailed(librarySectionKey);
        return librarySectionKey;
    }

    public void setLibrarySectionTitle(String librarySectionTitle) {
        this.librarySectionTitle = librarySectionTitle;
    }

    public void setLibrarySectionID(Integer librarySectionID) {
        this.librarySectionID = librarySectionID;
    }

    public void setLibrarySectionKey(String librarySectionKey) {
        this.librarySectionKey = librarySectionKey;
    }

    public abstract int typeId();

    public Boolean getTitleLocked() {
        return getFieldLocked("title");
    }

    public Boolean getSummaryLocked() {
        return getFieldLocked("summary");
    }

    public Boolean getTitleSortLocked() {
        return getFieldLocked("titleSort");
    }

    public void editTitle(String value, Optional<Boolean> lock) {
        editField("title.value", value);
        if (lock.isPresent())
            editField("title.locked", lock.get() ? "1" : "0");
    }

    public void editSummary(String value, Optional<Boolean> lock) {
        editField("summary.value", value);
        if (lock.isPresent())
            editField("summary.locked", lock.get() ? "1" : "0");
    }

    public void editTitleSort(String value, Optional<Boolean> lock) {
        editField("titleSort.value", value);
        if (lock.isPresent())
            editField("titleSort.locked", lock.get() ? "1" : "0");
    }
}
