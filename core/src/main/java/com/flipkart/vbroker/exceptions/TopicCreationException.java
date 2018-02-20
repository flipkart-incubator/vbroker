package com.flipkart.vbroker.exceptions;

public class TopicCreationException extends VBrokerException {

    public TopicCreationException() {
        super();
    }

    public TopicCreationException(String message) {
        super(message);
    }

    public TopicCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicCreationException(Throwable cause) {
        super(cause);
    }
}
