package com.flipkart.vbroker.exceptions;

public class BrokerUnAvailableException extends VBrokerException {
    public BrokerUnAvailableException() {
        super();
    }

    public BrokerUnAvailableException(String message) {
        super(message);
    }

    public BrokerUnAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokerUnAvailableException(Throwable cause) {
        super(cause);
    }
}
