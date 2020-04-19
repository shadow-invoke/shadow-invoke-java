package io.shadowstack.exceptions;

public class InvocationRecorderException extends Exception {
    private static final long serialVersionUID = -7740801425890056298L;

    public InvocationRecorderException(String message) {
        super(message);
    }

    public InvocationRecorderException(Throwable cause) {
        super(cause);
    }
}
