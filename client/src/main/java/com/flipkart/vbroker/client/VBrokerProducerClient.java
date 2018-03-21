package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class VBrokerProducerClient {

    @SuppressWarnings("SuspiciousToArrayCall")
    public static void main(String args[]) throws IOException {
        VBClientConfig config = VBClientConfig.newConfig("client.properties");
        log.info("Configs: ", config);

        Topic groupedTopic = DummyEntities.groupedTopic;
        produceDummyMessages(config, groupedTopic, "http://localhost:12000/messages");
    }

    public static void produceDummyMessages(VBClientConfig config, Topic topic, String httpUrl) {
        byte[] payload = "This is a sample message".getBytes();

        List<ProducerRecord> records = IntStream.range(0, 1)
            .mapToObj(i -> getProducerRecord(topic, "group_" + i, httpUrl, payload))
            .collect(Collectors.toList());

        try (Producer producer = new VBrokerProducer(config)) {
            List<CompletionStage<MessageMetadata>> resultStages = records.stream()
                .map(producer::produce)
                .collect(Collectors.toList());

            CompletableFuture[] completableFutures = new CompletableFuture[resultStages.size()];
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(resultStages.toArray(completableFutures));

            CompletableFuture<Void> resultFuture = allFuture.thenAccept(aVoid -> resultStages.stream()
                .map(stage -> stage.toCompletableFuture().join())
                .forEach(messageMetadata -> {
                    log.info("Message with msg_id {} got produced to topic {} and partition {}",
                        messageMetadata.getMessageId(),
                        messageMetadata.getTopicId(),
                        messageMetadata.getPartitionId());
                }));

            resultFuture.join();
        }

        log.info("Done producing. Closing client");
    }

    private static ProducerRecord getProducerRecord(Topic topic,
                                                    String group,
                                                    String httpUrl,
                                                    byte[] payload) {
        Message message = MessageStore.getRandomMsg(group);
        return ProducerRecord.builder()
            .groupId(message.groupId())
            .messageId(message.messageId())
            .crc((byte) 1)
            .version((byte) 1)
            .seqNo(1)
            .topicId(topic.id())
            .attributes(201)
            .httpUri(httpUrl)
            //.httpUri("http://localhost:12000/errors/500")
            //.httpUri("http://localhost:12000/messages")
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(topic.id())
            .callbackHttpUri("http://localhost:12000/messages")
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }
}
