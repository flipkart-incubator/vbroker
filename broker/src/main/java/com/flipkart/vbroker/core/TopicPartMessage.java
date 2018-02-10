package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TopicPartMessage {
    private final TopicPartition topicPartition;
    private final Message message;

    public static TopicPartMessage newInstance(TopicPartition topicPartition, Message message) {
        return new TopicPartMessage(topicPartition, message);
    }
}
