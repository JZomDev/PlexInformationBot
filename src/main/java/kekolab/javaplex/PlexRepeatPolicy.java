package kekolab.javaplex;

public enum PlexRepeatPolicy {
    NONE, ONE, ALL;

    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "0";
            case ONE:
                return "1";
            case ALL:
                return "2";
        }
        throw new PlexException(); // unreachable code
    }
}