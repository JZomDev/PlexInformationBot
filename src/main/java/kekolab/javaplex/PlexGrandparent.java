package kekolab.javaplex;

import java.util.List;

public interface PlexGrandparent<S extends PlexSection, C extends PlexMediatag<S>, GC extends PlexGrandchild<S, ?, ?>> extends PlexParent<S, C> {
    List<GC> grandchildren();
}