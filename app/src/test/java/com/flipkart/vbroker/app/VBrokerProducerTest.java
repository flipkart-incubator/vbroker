package com.flipkart.vbroker.app;

import com.flipkart.vbroker.client.*;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.Method;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.flipkart.vbroker.app.MockHttp.MockURI.URI_200;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;

@Slf4j
public class VBrokerProducerTest extends AbstractVBrokerBaseTest {

    @Test
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_Grouped() throws InterruptedException {
        int noOfRecords = 5;
        Topic topic = createGroupedTopic();
        byte[] payload = "This is a sample message".getBytes();
        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_0", payload))
            .collect(Collectors.toList());
        produceRecords(records);

        Thread.sleep(10 * 1000);
        //Verify the message is consumed
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_200.uri()));
    }

    @Test
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_UnGrouped() throws InterruptedException {
        int noOfRecords = 5;
        Topic topic = createUnGroupedTopic();
        byte[] payload = "This is a sample message".getBytes();
        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_" + i, payload))
            .collect(Collectors.toList());
        produceRecords(records);

        Thread.sleep(10 * 1000);
        //Verify the message is consumed
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_200.uri()));
    }

    private void produceRecords(List<ProducerRecord> records) {
        VBClientConfig config = getVbClientConfig();

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
    }

    private VBClientConfig getVbClientConfig() {
        Properties properties = new Properties();

        properties.setProperty("broker.host", "localhost");
        properties.setProperty("broker.port", BROKER_PORT + "");
        properties.setProperty("batch.size", "10240");
        properties.setProperty("linger.time.ms", String.valueOf(4000));
        properties.setProperty("metadata.expiry.time.ms", "6000");

        return new VBClientConfig(properties);
    }

    public ProducerRecord newProducerRecord(Topic topic, String group, byte[] payload) {
        Message message = MessageStore.getRandomMsg(group);
        return ProducerRecord.builder()
            .groupId(message.groupId())
            .messageId(message.messageId())
            .crc((byte) 1)
            .version((byte) 1)
            .seqNo(1)
            .topicId(topic.id())
            .attributes(201)
            .httpUri(MockHttp.MockURI.URI_200.url())
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(-1)
            .callbackHttpUri(MockHttp.MockURI.URI_200.url())
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }
}