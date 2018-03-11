package com.flipkart.vbroker.app;

import com.flipkart.vbroker.client.*;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.Lists;
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

import static com.flipkart.vbroker.app.MockHttp.MockURI.*;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.*;

@Slf4j
public class VBrokerProducerTest extends AbstractVBrokerBaseTest {
    private static final String payloadPrefix = "SampleMsg_";

    @Test
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_Grouped() throws InterruptedException {
        int noOfRecords = 5;
        Topic topic = createGroupedTopic();

        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_0", getMsgBody(payloadPrefix, i).getBytes()))
            .collect(Collectors.toList());
        produceRecords(records);

        Thread.sleep(2 * 1000);
        //Verify the message is consumed

        IntStream.range(0, noOfRecords)
            .forEachOrdered(i -> {
                log.info("Verify record {} at consuming end", i);
                verifyHttp(httpServer).times(1,
                    method(Method.POST), uri(URI_200.uri()), withPostBodyContaining(getMsgBody(payloadPrefix, i)));
            });
    }

    private String getMsgBody(String payloadPrefix, int i) {
        return String.format("%s_msg_%s", payloadPrefix, i);
    }

    @Test
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_MultipleGroups() throws InterruptedException {
        int noOfRecords = 3;
        Topic topic = createGroupedTopic();
        byte[] payload = "This is a sample message".getBytes();
        List<ProducerRecord> records_1 = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_1", MockHttp.MockURI.URI_200, payload))
            .collect(Collectors.toList());
        List<ProducerRecord> records_2 = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_2", MockHttp.MockURI.URI_201, payload))
            .collect(Collectors.toList());
        List<ProducerRecord> records_3 = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_3", URI_202, payload))
            .collect(Collectors.toList());

        List<ProducerRecord> records = Lists.newArrayList();
        records.addAll(records_1);
        records.addAll(records_2);
        records.addAll(records_3);

        produceRecords(records);

        Thread.sleep(4 * 1000);
        //Verify the message is consumed
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_200.uri()));
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_201.uri()));
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_202.uri()));
    }

    @Test
    public void shouldProduceAndConsumeMessages_WithCallback() throws InterruptedException {
        int noOfRecords = 5;
        Topic topic = createGroupedTopic();

        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecordWithCallback(topic, "group_0",
                MockHttp.MockURI.URI_200,
                topic.id(),
                MockHttp.MockURI.URI_201,
                getMsgBody(payloadPrefix, i).getBytes()))
            .collect(Collectors.toList());
        produceRecords(records);

        Thread.sleep(4 * 1000);

        //verify forward messages
        log.info("Verify forward messages at consuming end");
        verifyHttp(httpServer).times(noOfRecords,
            method(Method.POST), uri(URI_200.uri()), withPostBodyContaining(payloadPrefix));

        log.info("Verify callback messages at consuming end");
        verifyHttp(httpServer).times(noOfRecords,
            method(Method.POST), uri(URI_201.uri()));
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

        Thread.sleep(2 * 1000);
        //Verify the message is consumed
        verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
            uri(URI_200.uri()));
    }

    @Test
    public void shouldProduceAndConsumeMultipleMessages_GroupedAndUnGrouped() throws InterruptedException {
        int noOfRecords = 5;
        byte[] payload = "This is a sample message".getBytes();
        List<ProducerRecord> recordsGrouped = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(DummyEntities.groupedTopic, "group_0", payload))
            .collect(Collectors.toList());
        List<ProducerRecord> recordsUnGrouped = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(DummyEntities.unGroupedTopic, "group_" + i, payload))
            .collect(Collectors.toList());

        List<ProducerRecord> records = Lists.newArrayList(recordsGrouped);
        records.addAll(recordsUnGrouped);

        produceRecords(records);

        Thread.sleep(4 * 1000);
        //Verify the message is consumed
        verifyHttp(httpServer).times(noOfRecords * 2, method(Method.POST),
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
        properties.setProperty("linger.time.ms", String.valueOf(10));
        properties.setProperty("metadata.expiry.time.ms", "6000");

        return new VBClientConfig(properties);
    }

    private ProducerRecord newProducerRecord(Topic topic,
                                             String group,
                                             byte[] payload) {
        return newProducerRecord(topic, group, MockHttp.MockURI.URI_200, payload);
    }

    //without callbacks
    private ProducerRecord newProducerRecord(Topic topic,
                                             String group,
                                             MockHttp.MockURI mockURI,
                                             byte[] payload) {
        return newProducerRecordWithCallback(topic, group, mockURI, -1, mockURI, payload);
    }

    private ProducerRecord newProducerRecordWithCallback(Topic topic,
                                                         String group,
                                                         MockHttp.MockURI mockURI,
                                                         int callbackTopicId,
                                                         MockHttp.MockURI callbackMockURI,
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
            .httpUri(mockURI.url())
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(callbackTopicId)
            .callbackHttpUri(callbackMockURI.url())
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }
}