package kekolab.javaplex;

public class PlexException extends RuntimeException {
    public PlexException() {
        super();
    }

    public PlexException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlexException(Throwable cause) {
        super(cause);
    }

    public PlexException(String message) {
        super(message);
    }

    public PlexException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
