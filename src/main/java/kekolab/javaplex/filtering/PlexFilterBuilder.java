package kekolab.javaplex.filtering;

public class PlexFilterBuilder {
    public static PlexStringFilterBuilder when(PlexFilterableString field) {
        return new PlexStringFilterBuilder(field);
    }

    public static PlexIntegerFilterBuilder when(PlexFilterableInteger field) {
        return new PlexIntegerFilterBuilder(field);
    }

    public static PlexDateFilterBuilder when(PlexFilterableDate field) {
        return new PlexDateFilterBuilder(field);
    }

    public static PlexBooleanFilterBuilder when(PlexFilterableBoolean field) {
        return new PlexBooleanFilterBuilder(field);
    }

    public static PlexTagFilterBuilder when(PlexFilterableTag field) {
        return new PlexTagFilterBuilder(field);
    }

    public static PlexFilter or(PlexFilter... filters) {
        return new PlexCompositeFilter(PlexCompositeFilter.Operator.OR, filters);
    }

    public static PlexFilter and(PlexFilter... filters) {
        return new PlexCompositeFilter(PlexCompositeFilter.Operator.AND, filters);
    }
}