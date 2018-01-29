package com.flipkart.vbroker.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(exclude = {"grouped"})
@ToString
public class PartSubscription {
    private short id;
    @Setter
    private boolean grouped = false;
    private TopicPartition topicPartition;

    public PartSubscription(short id, TopicPartition topicPartition) {
        this.id = id;
        this.topicPartition = topicPartition;
    }
}
