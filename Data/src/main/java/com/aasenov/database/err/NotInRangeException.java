package com.aasenov.database.err;

/**
 * Use this exception to identify not in range values.
 */
public class NotInRangeException extends Exception {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public NotInRangeException() {
        super();
    }

    public NotInRangeException(String message) {
        super(message);
    }

    public NotInRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotInRangeException(Throwable cause) {
        super(cause);
    }

}
