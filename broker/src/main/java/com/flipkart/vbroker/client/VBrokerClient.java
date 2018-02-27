package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.DummyEntities;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerClient {

    public static void main(String args[]) throws InterruptedException, IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: ", config);

        Partitioner partitioner = new DefaultPartitioner();
        try (Producer producer = new VBrokerProducer(config, partitioner)) {
            Message message = MessageStore.getRandomMsg("group_1");
            Topic topic = DummyEntities.groupedTopic;

            CompletionStage<MessageMetadata> produceStage = producer.produce(message, topic);
            produceStage.thenAccept(messageMetadata -> {
                log.info("Message with msg_id {} and group_id {} got produced to topic {} and partition {}",
                    message.messageId(), message.groupId(),
                    messageMetadata.getTopicId(), messageMetadata.getPartitionId());
            }).toCompletableFuture().join();
        }
    }
}
