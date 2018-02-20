package com.flipkart.vbroker.exceptions;

public class SubscriptionCreationException extends VBrokerException {

    public SubscriptionCreationException() {
        super();
    }

    public SubscriptionCreationException(String message) {
        super(message);
    }

    public SubscriptionCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionCreationException(Throwable cause) {
        super(cause);
    }
}
