package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.vbroker.utils.MetricUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VBrokerProducer implements Producer {

    private final VBClientConfig config;
    private final Accumulator accumulator;
    private final NetworkClient networkClient;
    private final Sender sender;
    private final ListeningExecutorService senderExecutor;
    private final ListeningExecutorService senderCallbackExecutor;
    private final Timer produceMsgEnqueueTimer;

    public VBrokerProducer(VBClientConfig config,
                           Accumulator accumulator,
                           MetricRegistry metricRegistry) {
        this.accumulator = accumulator;
        this.config = config;
        log.info("Configs: ", config);
        networkClient = new NetworkClientImpl(metricRegistry);
        sender = new Sender(accumulator, accumulator.fetchMetadata(), networkClient, metricRegistry);
        senderExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        senderCallbackExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        ListenableFuture<?> senderFuture = senderExecutor.submit(sender);
        senderFuture.addListener(() -> {
            log.info("Sender is closed");
        }, senderCallbackExecutor);

        this.produceMsgEnqueueTimer = metricRegistry.timer(MetricUtils.clientFullMetricName("produce.msg.enqueue.time"));
    }

    public VBrokerProducer(VBClientConfig config) {
        this(config, new Accumulator(config, new DefaultPartitioner()), new MetricRegistry());
    }

    @Override
    public CompletionStage<MessageMetadata> produce(ProducerRecord record) {
        try (Timer.Context ignored = produceMsgEnqueueTimer.time()) {
            return accumulator.accumulateRecord(record);
        }
    }

    @Override
    public void close() {
        log.info("Closing VBrokerProducer");
        sender.stop();

        try {
            networkClient.close();
        } catch (IOException e) {
            log.error("Exception in closing NetworkClient", e);
        }

        MoreExecutors.shutdownAndAwaitTermination(senderExecutor,
            config.getLingerTimeMs(), TimeUnit.MILLISECONDS);
        MoreExecutors.shutdownAndAwaitTermination(senderCallbackExecutor,
            config.getLingerTimeMs(), TimeUnit.MILLISECONDS);

        log.info("One-min rate is {} to produce {} records", produceMsgEnqueueTimer.getOneMinuteRate(), produceMsgEnqueueTimer.getCount());
        log.info("VBrokerProducer closed");
    }
}
