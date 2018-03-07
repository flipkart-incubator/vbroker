package com.flipkart.vbroker.app;

import com.flipkart.vbroker.client.*;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.Method;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;

@Slf4j
public class VBrokerProducerTest extends AbstractVBrokerBaseTest {

    @SuppressWarnings("SuspiciousToArrayCall")
    @Test
    public void shouldProduceAndConsumeMessage() {

        log.info("Broker Port: {}", BROKER_PORT);

        Properties properties = new Properties();
        properties.setProperty("broker.host", "localhost");
        properties.setProperty("broker.port", BROKER_PORT + "");
        properties.setProperty("batch.size", "10240");
        properties.setProperty("linger.time.ms", String.valueOf(4000));
        properties.setProperty("metadata.expiry.time.ms", "6000");

        VBClientConfig config = new VBClientConfig(properties);
        log.info("Configs: {}", config);

        Topic groupedTopic = DummyEntities.groupedTopic;
        byte[] payload = "This is a sample message".getBytes();

        List<ProducerRecord> records = IntStream.range(0, 1)
            .mapToObj(i -> newProducerRecord(groupedTopic, "group_" + i, payload))
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

        //Verify the message is consumed
        verifyHttp(httpServer).once(method(Method.POST),
            uri("/messages"));
    }
}