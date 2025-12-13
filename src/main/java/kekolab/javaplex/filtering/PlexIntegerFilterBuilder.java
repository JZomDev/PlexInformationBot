package kekolab.javaplex.filtering;

import kekolab.javaplex.filtering.PlexBaseFilter.Operator;

public class PlexIntegerFilterBuilder extends PlexFilterBuilder {
    private final PlexFilterableInteger field;

    protected PlexIntegerFilterBuilder(PlexFilterableInteger field) {
        this.field = field;
    }

    public PlexFilter equals(int value) {
        return new PlexBaseFilter(field.getName(), Operator.IS_OR_CONTAINS, Integer.toString(value));
    }

    public PlexFilter doesNotEqual(int value) {
        return new PlexBaseFilter(field.getName(), Operator.IS_NOT_OR_DOES_NOT_CONTAIN, Integer.toString(value));
    }

    public PlexFilter isGreaterThan(int value) {
        return new PlexBaseFilter(field.getName(), Operator.IS_GREATER_THAN, Integer.toString(value));
    }

    public PlexFilter isLessthan(int value) {
        return new PlexBaseFilter(field.getName(), Operator.IS_LESS_THAN, Integer.toString(value));
    }
}