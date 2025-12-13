package kekolab.javaplex;

import java.net.URI;

public interface PlexChild<S extends PlexSection, P extends PlexMediatag<S>> {
    P parent();

    String getParentGuid();

    Integer getParentIndex();

    String getParentKey();

    URI parentKey();

    Integer getParentRatingKey();

    URI parentRatingKey();

    String getParentStudio();

    String getParentTheme();

    URI parentTheme();

    String getParentThumb();

    URI parentThumb();

    String getParentTitle();

    Integer getParentYear();

}
