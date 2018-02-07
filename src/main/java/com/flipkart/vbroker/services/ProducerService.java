package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.core.TopicPartitionDataManager;
import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProducerService {

    private final TopicPartitionDataManager topicPartitionDataManager;

    public void produceMessage(TopicPartition topicPartition, Message message) {
        log.info("Producing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
        topicPartitionDataManager.addMessage(topicPartition, message);
    }
}
