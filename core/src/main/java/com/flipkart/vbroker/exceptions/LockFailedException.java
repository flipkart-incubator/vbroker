package com.flipkart.vbroker.exceptions;

public class LockFailedException extends VBrokerException {
    public LockFailedException() {
        super();
    }

    public LockFailedException(String message) {
        super(message);
    }

    public LockFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockFailedException(Throwable cause) {
        super(cause);
    }
}
