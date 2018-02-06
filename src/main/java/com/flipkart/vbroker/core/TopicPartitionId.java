package com.flipkart.vbroker.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class TopicPartitionId {
    private short topicId;
    private short partitionId;
}
