package com.flipkart.vbroker.client;

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

    public VBrokerProducer(VBClientConfig config,
                           Accumulator accumulator) {
        this.accumulator = accumulator;
        this.config = config;
        log.info("Configs: ", config);
        networkClient = new NetworkClientImpl();
        sender = new Sender(accumulator, accumulator.fetchMetadata(), networkClient);
        senderExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        senderCallbackExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        ListenableFuture<?> senderFuture = senderExecutor.submit(sender);
        senderFuture.addListener(() -> {
            log.info("Sender is closed");
        }, senderCallbackExecutor);
    }

    public VBrokerProducer(VBClientConfig config) {
        this(config, new Accumulator(config, new DefaultPartitioner()));
    }

    @Override
    public CompletionStage<MessageMetadata> produce(ProducerRecord record) {
        return accumulator.accumulateRecord(record);
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

        log.info("VBrokerProducer closed");
    }
}
