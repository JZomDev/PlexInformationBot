package kekolab.javaplex.filtering;

import kekolab.javaplex.filtering.PlexBaseFilter.Operator;

public class PlexBooleanFilterBuilder extends PlexFilterBuilder {
    private final PlexFilterableBoolean field;

    protected PlexBooleanFilterBuilder(PlexFilterableBoolean field) {
        this.field = field;
    }

    public PlexFilter isTrue() {
        return new PlexBaseFilter(field.getName(), Operator.IS_OR_CONTAINS, "1");
    }

    public PlexFilter isFalse() {
        return new PlexBaseFilter(field.getName(), Operator.IS_NOT_OR_DOES_NOT_CONTAIN, "1");
    }
}