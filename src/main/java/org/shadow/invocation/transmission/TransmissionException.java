package org.shadow.invocation.transmission;

public class TransmissionException extends Exception {
    public TransmissionException() {
        super();
    }

    public TransmissionException(String message) {
        super(message);
    }

    public TransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransmissionException(Throwable cause) {
        super(cause);
    }

    protected TransmissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
