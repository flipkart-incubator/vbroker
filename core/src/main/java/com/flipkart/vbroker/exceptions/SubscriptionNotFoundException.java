package com.flipkart.vbroker.exceptions;

public class SubscriptionNotFoundException extends VBrokerException {
    public SubscriptionNotFoundException() {
        super();
    }

    public SubscriptionNotFoundException(String message) {
        super(message);
    }

    public SubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionNotFoundException(Throwable cause) {
        super(cause);
    }
}
