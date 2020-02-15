package org.shadow.exception;

public class ReplayException extends Exception {
    public ReplayException() {
        super();
    }

    public ReplayException(String message) {
        super(message);
    }

    public ReplayException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReplayException(Throwable cause) {
        super(cause);
    }

    protected ReplayException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
