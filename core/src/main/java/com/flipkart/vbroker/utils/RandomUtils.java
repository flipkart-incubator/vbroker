package com.flipkart.vbroker.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
public class RandomUtils {

    private static final Random correlationIdRandom = new Random(10000);
    private static final Random topicIdRandom = new Random(10000);
    private static final Random subscriptionIdRandom = new Random(10000);

    public static int generateRandomCorrelationId() {
        return Math.abs(correlationIdRandom.nextInt());
    }

    public static int generateRandomTopicId() {
        return Math.abs(topicIdRandom.nextInt());
    }

    public static int generateRandomSubscriptionId() {
        return Math.abs(subscriptionIdRandom.nextInt());
    }

    public static void main(String[] args) {
        IntStream.range(0, 10)
            .forEachOrdered(i -> {
                int correlationId = generateRandomCorrelationId();
                log.info("CorrelationId: {}", correlationId);
            });
    }
}
