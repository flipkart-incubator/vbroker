package com.flipkart.vbroker.exceptions;

public class DuplicateTopicException extends VBrokerException {

    public DuplicateTopicException() {
        super();
    }

    public DuplicateTopicException(String message) {
        super(message);
    }

    public DuplicateTopicException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateTopicException(Throwable cause) {
        super(cause);
    }
}
