package kekolab.javaplex;

import java.net.URI;

public class PlexUser extends PlexBaseItem {
    private String id;
    private String title;

    private UriProvider thumb;

    public PlexUser() {
        thumb = new UriProvider(null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThumb(String thumb) {
        this.thumb.setValue(thumb);
    }

    public String getThumb() {
        return (String) thumb.getValue();
    }

    public URI thumb() {
        return thumb.uri();
    }

}
