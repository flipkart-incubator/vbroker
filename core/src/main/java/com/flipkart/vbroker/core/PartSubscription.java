package com.flipkart.vbroker.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(exclude = {"grouped"})
@ToString
@AllArgsConstructor
public class PartSubscription {
    private final int id;
    private final TopicPartition topicPartition;
    private final int subscriptionId;
    private boolean grouped;
}
