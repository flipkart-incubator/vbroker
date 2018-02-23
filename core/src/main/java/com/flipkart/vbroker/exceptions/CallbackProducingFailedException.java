package com.flipkart.vbroker.exceptions;

public class CallbackProducingFailedException extends VBrokerException {
    public CallbackProducingFailedException() {
        super();
    }

    public CallbackProducingFailedException(String message) {
        super(message);
    }

    public CallbackProducingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallbackProducingFailedException(Throwable cause) {
        super(cause);
    }
}
