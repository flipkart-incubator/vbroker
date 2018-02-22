package com.flipkart.vbroker.services;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.TopicUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Slf4j
@AllArgsConstructor
public class ProducerService {

    private final TopicPartDataManager topicPartDataManager;

    //TODO: make this return IterableMessage which contains where the message is produced
    public CompletionStage<MessageMetadata> produceMessage(TopicPartMessage topicPartMessage) {
        Message message = topicPartMessage.getMessage();
        TopicPartition topicPartition = topicPartMessage.getTopicPartition();
        log.info("Producing message with msg_id: {} and group_id: {} to topic {} and partition {}",
            message.messageId(), message.groupId(), topicPartition.getTopicId(), topicPartition.getId());
        return topicPartDataManager.addMessage(topicPartition, message);
    }

    public CompletionStage<List<MessageMetadata>> produceMessages(List<TopicPartMessage> topicPartMessages) {
        topicPartMessages.forEach(topicPartMessage -> {
            Message message = topicPartMessage.getMessage();
            TopicPartition topicPartition = topicPartMessage.getTopicPartition();
            log.info("BulkProducing message with msg_id: {} and group_id: {} to topic {} and partition {}",
                message.messageId(), message.groupId(), topicPartition.getTopicId(), topicPartition.getId());
        });
        return topicPartDataManager.addMessages(topicPartMessages);
    }

    public CompletionStage<MessageMetadata> produceMessage(Topic topic, Message message) {
        //TODO: do the partitioning logic correctly - currently defaulting to 1st partition always
        TopicPartition topicPartition = TopicUtils.getTopicPartitions(topic).get(0);
        TopicPartMessage topicPartMessage = TopicPartMessage.newInstance(topicPartition, message);
        return produceMessage(topicPartMessage);
    }
}
