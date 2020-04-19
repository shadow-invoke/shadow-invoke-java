package io.shadowstack.exceptions;

public class InvocationSourceException extends Exception {
    private static final long serialVersionUID = 2722732652625740973L;

    public InvocationSourceException() {
        super();
    }

    public InvocationSourceException(String message) {
        super(message);
    }

    public InvocationSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationSourceException(Throwable cause) {
        super(cause);
    }
}
