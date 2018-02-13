package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.TopicUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class ProducerService {

    private final TopicPartDataManager topicPartDataManager;

    //TODO: make this return MessageMetadata which contains where the message is produced
    public void produceMessage(TopicPartition topicPartition, Message message) {
        log.info("Producing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
        topicPartDataManager.addMessage(topicPartition, message);
    }

    public void produceMessages(List<TopicPartMessage> topicPartMessages) {
        for (TopicPartMessage topicPartMessage : topicPartMessages) {
            produceMessage(topicPartMessage.getTopicPartition(), topicPartMessage.getMessage());
        }
    }

    public void produceMessage(Topic topic, Message message) {
        //TODO: do this correctly
        produceMessage(TopicUtils.getTopicPartitions(topic.topicId(), (short) 1).get(0), message);
    }
}
