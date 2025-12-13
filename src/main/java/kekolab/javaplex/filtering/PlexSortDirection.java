package kekolab.javaplex.filtering;

import kekolab.javaplex.PlexException;

public enum PlexSortDirection {
    ASC, DESC;

    @Override
    public String toString() {
        switch (this) {
            case ASC:
                return "asc";
            case DESC:
                return "desc";
        }
        throw new PlexException("Cannot stringify enum constant " + name());
    }

    public static PlexSortDirection parse(String string) {
        if (string.equalsIgnoreCase("asc"))
            return ASC;
        if (string.equalsIgnoreCase("desc"))
            return DESC;
        throw new PlexException("Unrecognised sorting order: " + string);
    }
}