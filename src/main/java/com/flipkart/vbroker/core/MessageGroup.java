package com.flipkart.vbroker.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@EqualsAndHashCode
public class MessageGroup {
    private final String groupId;
    private final TopicPartition topicPartition;

    public MessageGroup(String groupId, TopicPartition topicPartition) {
        this.groupId = groupId;
        this.topicPartition = topicPartition;
    }
}
