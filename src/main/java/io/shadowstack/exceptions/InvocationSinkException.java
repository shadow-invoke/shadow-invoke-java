package io.shadowstack.exceptions;

public class InvocationSinkException extends Exception {
    private static final long serialVersionUID = 498562007410183620L;

    public InvocationSinkException() {
        super();
    }

    public InvocationSinkException(String message) {
        super(message);
    }

    public InvocationSinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationSinkException(Throwable cause) {
        super(cause);
    }
}
