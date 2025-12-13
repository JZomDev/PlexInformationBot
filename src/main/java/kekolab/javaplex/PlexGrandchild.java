package kekolab.javaplex;

import java.net.URI;

public interface PlexGrandchild<S extends PlexSection, P extends PlexMediatag<S>, GP extends PlexMediatag<S>> extends PlexChild<S, P> {
    GP grandparent();

    String getGrandparentArt();

    URI grandparentArt();

    String getGrandparentGuid();

    String getGrandparentKey();

    URI grandparentKey();

    Integer getGrandparentRatingKey();

    URI grandparentRatingKey();

    String getGrandparentTheme();

    URI grandparentTheme();

    String getGrandparentThumb();

    URI grandparentThumb();

    String getGrandparentTitle();

    Integer getGrandparentYear();
}