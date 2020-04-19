package io.shadowstack.exceptions;

public class InvocationKeyException extends Exception {
    private static final long serialVersionUID = 2198282273111427072L;

    public InvocationKeyException() {
        super();
    }

    public InvocationKeyException(String message) {
        super(message);
    }

    public InvocationKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationKeyException(Throwable cause) {
        super(cause);
    }
}
