package kekolab.javaplex;

import java.util.List;

public interface PlexParent<S extends PlexSection, C extends PlexMediatag<S>> {
    List<C> children();
}