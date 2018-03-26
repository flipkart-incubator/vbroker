package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.MetricUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class VBrokerProducerBenchmark {

    @SuppressWarnings("SuspiciousToArrayCall")
    public static void main(String args[]) throws IOException {
        VBClientConfig config = VBClientConfig.newConfig("client.properties");
        log.info("Configs: ", config);

        log.info("args: {}", Arrays.asList(args));

        int noOfMessagesPerIteration = Ints.tryParse(args[0]);
        int noOfIterations = Ints.tryParse(args[1]);
        String httpUri = args[2];
        String payload = args[3];
        boolean isBlocking = Boolean.parseBoolean(args[4]);

        Topic topic = DummyEntities.groupedTopic;

        MetricRegistry metricRegistry = new MetricRegistry();

        Timer timer = metricRegistry.timer(MetricUtils.clientFullMetricName("producer.benchmark.time"));
        Timer.Context context = timer.time();

        try (Producer producer = new VBrokerProducer(config)) {
            List<CompletableFuture> resultFutureList = Lists.newArrayList();

            IntStream.range(0, noOfIterations)
                .forEach(iteration -> {
                    List<ProducerRecord> records = IntStream.range(0, noOfMessagesPerIteration)
                        .mapToObj(i -> getProducerRecord(topic, "group_" + iteration, httpUri, payload.getBytes()))
                        .collect(Collectors.toList());

                    List<CompletionStage<MessageMetadata>> resultStages = records.stream()
                        .map(producer::produce)
                        .collect(Collectors.toList());

                    CompletableFuture[] completableFutures = new CompletableFuture[resultStages.size()];
                    CompletableFuture<Void> allFuture = CompletableFuture.allOf(resultStages.toArray(completableFutures));
                    CompletableFuture<Void> resultFuture = allFuture.thenAccept(aVoid -> resultStages.stream()
                        .map(stage -> stage.toCompletableFuture().join())
                        .forEach(messageMetadata -> {
                            log.debug("Message with msg_id {} got produced to topic {} and partition {}",
                                messageMetadata.getMessageId(),
                                messageMetadata.getTopicId(),
                                messageMetadata.getPartitionId());
                        }));

                    resultFutureList.add(resultFuture);
                });

            CompletableFuture[] resultFutures = resultFutureList.toArray(new CompletableFuture[resultFutureList.size()]);
            CompletableFuture<Void> finalResultFuture = CompletableFuture.allOf(resultFutures);

            if (isBlocking) {
                finalResultFuture.join();
            }
        }

        log.info("Mean rate for producing {} messages is {}", timer.getCount(), timer.getMeanRate());
        long elapsedTimeNs = context.stop();
        log.info("Total time taken to produce {} messages is {}ms", noOfMessagesPerIteration * noOfIterations, elapsedTimeNs / Math.pow(10, 6));
        log.info("Mean rate for producing {} messages is {}", timer.getCount(), timer.getMeanRate());

        //timer.get
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
            //.callbackTopicId(topic.id())
            .callbackTopicId(-1)
            .callbackHttpUri("http://localhost:12000/messages")
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }
}
