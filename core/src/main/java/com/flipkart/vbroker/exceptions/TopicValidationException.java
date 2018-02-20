package com.flipkart.vbroker.exceptions;

public class TopicValidationException extends VBrokerException {

    public TopicValidationException() {
        super();
    }

    public TopicValidationException(String message) {
        super(message);
    }

    public TopicValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicValidationException(Throwable cause) {
        super(cause);
    }
}
