package com.flipkart.vbroker.core;

import com.flipkart.vbroker.core.TopicPartition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(exclude = {"grouped"})
@ToString
public class PartSubscription {
    private final short id;
    private final TopicPartition topicPartition;
    private final short subscriptionId;
    @Setter
    private boolean grouped = false;

    public PartSubscription(short id,
                            TopicPartition topicPartition,
                            short subscriptionId) {
        this.id = id;
        this.topicPartition = topicPartition;
        this.subscriptionId = subscriptionId;
    }
}
