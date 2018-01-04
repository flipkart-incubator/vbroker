package com.flipkart.vbroker.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerException extends RuntimeException {

    public VBrokerException() {
        super();
    }

    public VBrokerException(String message) {
        super(message);
        //log.error("Exception at VBroker: {}", message);
    }

    public VBrokerException(String message, Throwable cause) {
        super(message, cause);
    }

    public VBrokerException(Throwable cause) {
        super(cause);
    }
}
