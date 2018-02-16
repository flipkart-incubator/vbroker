package com.flipkart.vbroker.controller;

/**
 * @author govind.ajith
 */
public interface AllocationStrategy {

    /**
     * Allocate to brokers.
     */
    public void allocate();
}
