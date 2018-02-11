package com.flipkart.vbroker.exceptions;

public class TopicNotFoundException extends VBrokerException {
    public TopicNotFoundException() {
        super();
    }

    public TopicNotFoundException(String message) {
        super(message);
    }

    public TopicNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicNotFoundException(Throwable cause) {
        super(cause);
    }
}
