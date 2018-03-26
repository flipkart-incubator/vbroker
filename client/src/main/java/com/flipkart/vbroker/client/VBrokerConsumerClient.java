package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerConsumerClient {

    public static void main(String[] args) throws Exception {
        VBClientConfig config = VBClientConfig.newConfig("client.properties");
        log.info("Configs: ", config);

        if (true) {
            throw new RuntimeException("Produce messages here to consume");
        }
//        VBrokerProducerBenchmark.produceDummyMessages(
//            config,
//            DummyEntities.groupedTopic,
//            "http://localhost:12000/errors/404");

        Set<Subscription> subscriptions = Sets.newHashSet(DummyEntities.groupedSubscription);
        VBrokerConsumer consumer = new VBrokerConsumer(config, new MetricRegistry());
        try {
            consumer.subscribe(subscriptions);

            int maxRecords = 10;
            int timeoutMs = 2000;
            CompletionStage<List<ConsumerRecord>> records = consumer.poll(maxRecords, timeoutMs);

            records.thenAccept(consumerRecords -> {
                log.info("No of records consumed are: {}", consumerRecords.size());
                consumerRecords.forEach(consumerRecord -> {
                    log.info("Consumed record with msg_id: {} and grp_id: {}",
                        consumerRecord.getMessageId(), consumerRecord.getGroupId());
                });
            }).toCompletableFuture().join();
        } finally {
            consumer.unSubscribe(subscriptions);
            consumer.close();
        }
    }
}
