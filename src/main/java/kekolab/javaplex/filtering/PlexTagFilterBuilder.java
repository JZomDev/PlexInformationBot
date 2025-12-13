package kekolab.javaplex.filtering;

import kekolab.javaplex.PlexSectionSecondaryDirectory;
import kekolab.javaplex.PlexTag;
import kekolab.javaplex.filtering.PlexBaseFilter.Operator;

public class PlexTagFilterBuilder extends PlexFilterBuilder {
    private final PlexFilterableTag field;

    protected PlexTagFilterBuilder(PlexFilterableTag field) {
        this.field = field;
    }

    public PlexFilter is(PlexSectionSecondaryDirectory<?> directory) {
        return new PlexBaseFilter(field.getName(), Operator.IS_OR_CONTAINS, directory.getKey());
    }

    public PlexFilter isNot(PlexSectionSecondaryDirectory<?> directory) {
        return new PlexBaseFilter(field.getName(), Operator.IS_NOT_OR_DOES_NOT_CONTAIN, directory.getKey());
    }

    public PlexFilter is(PlexTag tag) {
        return new PlexBaseFilter(field.getName(), Operator.IS_OR_CONTAINS, Integer.toString(tag.getId()));
    }

    public PlexFilter isNot(PlexTag tag) {
        return new PlexBaseFilter(field.getName(), Operator.IS_NOT_OR_DOES_NOT_CONTAIN, Integer.toString(tag.getId()));
    }
}