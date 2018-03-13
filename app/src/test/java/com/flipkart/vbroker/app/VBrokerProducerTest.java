package com.flipkart.vbroker.app;

import com.flipkart.vbroker.app.MockHttp.MockURI;
import com.flipkart.vbroker.client.*;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.Lists;
import com.xebialabs.restito.builder.verify.VerifySequenced;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.Method;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.flipkart.vbroker.app.MockHttp.MOCK_HTTP_SERVER_PORT;
import static com.flipkart.vbroker.app.MockHttp.MockURI.URI_200;
import static com.flipkart.vbroker.app.MockHttp.MockURI.URI_201;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.*;
import static org.testng.Assert.assertTrue;

@Slf4j
public class VBrokerProducerTest extends AbstractVBrokerBaseTest {
    private static final String payloadPrefix = "SampleMsg_";

    @Test(invocationCount = 5)
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_Grouped() throws InterruptedException, MalformedURLException {
        produceAndConsumeMessages_ValidateConsumingSequence(MockURI.URI_200, 0);
    }

    @Test
    public void shouldProduceAndConsumeMessage_InOrderForSameGroup_WithSlowDestinations() throws InterruptedException, MalformedURLException {
        produceAndConsumeMessages_ValidateConsumingSequence(MockURI.SLEEP_200, 4000);
    }

    private void produceAndConsumeMessages_ValidateConsumingSequence(MockURI mockURI, int sleepTimeMs) throws InterruptedException, MalformedURLException {
        int noOfRecords = 3;
        Topic topic = createGroupedTopic();

        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecord(topic, "group_0", mockURI, getMsgBody(payloadPrefix, i).getBytes()))
            .collect(Collectors.toList());
        produceRecords(records);

        Thread.sleep(sleepTimeMs);
        //Verify the message is consumed

        validateSequence(records);
    }

    private void validateSequence(List<ProducerRecord> records) throws MalformedURLException {
        //noinspection ConstantConditions
        assertTrue(records.size() >= 1);

        ProducerRecord record = records.get(0);
        String body = getBody(record);
        URL url = new URL(record.getHttpUri());
        VerifySequenced verifySequenced = verifyHttp(httpServer).times(1,
            method(Method.POST), uri(url.getPath()), withPostBodyContaining(body));

        for (int i = 1; i < records.size(); i++) {
            record = records.get(i);
            body = new String(record.getPayload());
            url = new URL(record.getHttpUri());
            verifySequenced = verifySequenced.then().times(1,
                method(Method.POST), uri(url.getPath()), withPostBodyContaining(body));
        }
    }

    private String getBody(ProducerRecord record) {
        return new String(record.getPayload());
    }

    private String getMsgBody(String payloadPrefix, int i) {
        return String.format("%s_msg_%s", payloadPrefix, i);
    }

    @Test(invocationCount = 15)
    public void shouldProduceAndConsumeMessages_MultipleMessagesOfSameTopic_MultipleGroups() throws InterruptedException {
        int noOfRecords = 10;
        int noOfGroups = 50;

        Topic topic = createGroupedTopic();
        byte[] payload = "This is a sample message".getBytes();

        List<ProducerRecord> allRecords = Lists.newArrayList();
        IntStream.range(0, noOfGroups)
            .forEachOrdered(groupIdx -> {
                List<ProducerRecord> records = IntStream.range(0, noOfRecords)
                    .mapToObj(i -> newProducerRecordWithCallback(topic, "group_" + groupIdx, getUrl(groupIdx),
                        -1, getUrl(groupIdx), payload))
                    .collect(Collectors.toList());
                allRecords.addAll(records);
            });

        produceRecords(allRecords);
        Thread.sleep(2 * 1000);

        IntStream.range(0, noOfGroups)
            .forEachOrdered(groupIdx -> {
                log.info("GroupIdx {}", groupIdx);
                verifyHttp(httpServer).times(noOfRecords, method(Method.POST),
                    uri(getUri(groupIdx)));
            });
    }

    private String getUri(int groupIdx) {
        return "/" + String.valueOf(250 + groupIdx);
    }

    private String getUrl(int groupIdx) {
        String uri = getUri(groupIdx);
        return String.format("http://localhost:%d%s", MOCK_HTTP_SERVER_PORT, uri);
    }

    @Test
    public void shouldProduceAndConsumeMessages_WithCallback() throws InterruptedException {
        int noOfRecords = 3;
        Topic topic = createGroupedTopic();

        List<ProducerRecord> records = IntStream.range(0, noOfRecords)
            .mapToObj(i -> newProducerRecordWithCallback(topic, "group_0",
                MockURI.URI_200,
                topic.id(),
                MockURI.URI_201,
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
        return newProducerRecord(topic, group, MockURI.URI_200, payload);
    }

    //without callbacks
    private ProducerRecord newProducerRecord(Topic topic,
                                             String group,
                                             MockURI mockURI,
                                             byte[] payload) {
        return newProducerRecordWithCallback(topic, group, mockURI, -1, mockURI, payload);
    }

    private ProducerRecord newProducerRecordWithCallback(Topic topic,
                                                         String group,
                                                         String mockURI,
                                                         int callbackTopicId,
                                                         String callbackMockURI,
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
            .httpUri(mockURI)
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(callbackTopicId)
            .callbackHttpUri(callbackMockURI)
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }

    private ProducerRecord newProducerRecordWithCallback(Topic topic,
                                                         String group,
                                                         MockURI mockURI,
                                                         int callbackTopicId,
                                                         MockURI callbackMockURI,
                                                         byte[] payload) {
        return newProducerRecordWithCallback(topic, group, mockURI.uri(), callbackTopicId, callbackMockURI.uri(), payload);
    }

}