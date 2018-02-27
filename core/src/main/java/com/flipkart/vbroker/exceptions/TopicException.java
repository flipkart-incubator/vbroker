package com.flipkart.vbroker.exceptions;

public class TopicException extends VBrokerException {

    public TopicException() {
        super();
    }

    public TopicException(String message) {
        super(message);
    }

    public TopicException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicException(Throwable cause) {
        super(cause);
    }
}
