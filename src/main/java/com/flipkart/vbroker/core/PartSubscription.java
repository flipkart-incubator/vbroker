package com.flipkart.vbroker.core;

import lombok.Getter;

@Getter
public class PartSubscription {
    private short id;
    private boolean grouped = false;
    private TopicPartition topicPartition;
}
