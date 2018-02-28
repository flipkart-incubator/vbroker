package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerProducerClient {

    public static void main(String args[]) throws InterruptedException, IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: ", config);

        Partitioner partitioner = new DefaultPartitioner();
        try (Producer producer = new VBrokerProducer(config, partitioner)) {
            Message message = MessageStore.getRandomMsg("group_1");
            //Topic topic = DummyEntities.groupedTopic;

            ProducerRecord record = ProducerRecord.builder()
                .groupId(message.groupId())
                .messageId(message.messageId())
                .build();
            CompletionStage<MessageMetadata> produceStage = producer.produce(record);

            produceStage.thenAccept(messageMetadata -> {
                log.info("Message with msg_id {} and group_id {} got produced to topic {} and partition {}",
                    message.messageId(), message.groupId(),
                    messageMetadata.getTopicId(), messageMetadata.getPartitionId());
            }).toCompletableFuture().join();
        }
    }
}
