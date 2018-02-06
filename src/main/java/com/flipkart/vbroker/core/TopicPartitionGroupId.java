package com.flipkart.vbroker.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class TopicPartitionGroupId {
    private TopicPartitionId topicPartitionId;
    private String groupId;
}
