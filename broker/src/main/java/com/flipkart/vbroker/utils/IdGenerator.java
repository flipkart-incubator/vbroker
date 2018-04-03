package com.flipkart.vbroker.utils;

public class IdGenerator {

    public static int randomTopicId() {
        return RandomUtils.generateRandomTopicId();
    }

    public static int randomSubscriptionId() {
        return RandomUtils.generateRandomSubscriptionId();
    }
    
    public static int randomQueueId() {
        return RandomUtils.generateRandomTopicId();
    }
}
