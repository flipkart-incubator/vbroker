package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.DummyEntities;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerProducerClient {

    public static void main(String args[]) throws InterruptedException, IOException {

        VBClientConfig config = VBClientConfig.newConfig("client.properties");
        log.info("Configs: ", config);

        Topic groupedTopic = DummyEntities.groupedTopic;
        byte[] payload = "This is a sample message".getBytes();

        try (Producer producer = new VBrokerProducer(config)) {
            Message message = MessageStore.getRandomMsg("group_1");
            //Topic topic = DummyEntities.groupedTopic;

            ProducerRecord record = ProducerRecord.builder()
                .groupId(message.groupId())
                .messageId(message.messageId())
                .crc((byte) 1)
                .version((byte) 1)
                .seqNo(1)
                .topicId(groupedTopic.id())
                .attributes(201)
                .httpUri("http://localhost:12000/messages")
                .httpMethod(ProducerRecord.HttpMethod.POST)
                .callbackTopicId(groupedTopic.id())
                .callbackHttpUri("http://localhost:12000/messages")
                .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
                .headers(new HashMap<>())
                .payload(payload)
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
