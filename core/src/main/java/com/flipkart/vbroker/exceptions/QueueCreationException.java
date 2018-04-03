package com.flipkart.vbroker.exceptions;

public class QueueCreationException extends VBrokerException {

    public QueueCreationException() {
        super();
    }

    public QueueCreationException(String message) {
        super(message);
    }

    public QueueCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueCreationException(Throwable cause) {
        super(cause);
    }
}
