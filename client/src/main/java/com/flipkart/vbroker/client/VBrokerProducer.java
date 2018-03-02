package com.flipkart.vbroker.client;

import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.*;

@Slf4j
public class VBrokerProducer implements Producer {

    private final VBClientConfig config;
    //private final Partitioner partitioner;
    private final Accumulator accumulator;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final Sender sender;
    private final ListeningExecutorService senderExecutor;
    private final ListeningExecutorService senderCallbackExecutor;

    public VBrokerProducer(VBClientConfig config,
                           Partitioner partitioner,
                           Accumulator accumulator) {
        //this.partitioner = partitioner;
        this.accumulator = accumulator;

        this.config = config;
        log.info("Configs: ", config);
        group = new NioEventLoopGroup();
        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);

        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new VBrokerClientInitializer(responseHandlerFactory));

        sender = new Sender(accumulator, accumulator.fetchMetadata(), bootstrap, config);
        senderExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        senderCallbackExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        ListenableFuture<?> senderFuture = senderExecutor.submit(sender);
        senderFuture.addListener(() -> {
            log.info("Sender is closed");
        }, senderCallbackExecutor);
    }

    public VBrokerProducer(VBClientConfig config) {
        this(config, new DefaultPartitioner(), new Accumulator(config));
    }

    @Override
    public CompletionStage<MessageMetadata> produce(ProducerRecord record) {
        accumulator.addRecord(record);
        return getResponse(record);
    }

    private CompletionStage<MessageMetadata> getResponse(ProducerRecord record) {
        //TODO: fill this up correctly
        return CompletableFuture.supplyAsync(() ->
            new MessageMetadata(record.getMessageId(),
                record.getTopicId(),
                record.getPartitionId(),
                new Random().nextInt()));
    }

    @Override
    public void close() {
        log.info("Closing VBrokerProducer");
        sender.stop();

        try {
            group.shutdownGracefully().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception in shutting down the event loop group", e);
        }

        MoreExecutors.shutdownAndAwaitTermination(senderExecutor,
            config.getLingerTimeMs() * 5, TimeUnit.MILLISECONDS);
        MoreExecutors.shutdownAndAwaitTermination(senderCallbackExecutor,
            config.getLingerTimeMs() * 5, TimeUnit.MILLISECONDS);

        log.info("VBrokerProducer closed");
    }

    private CompletableFuture<Channel> convert(ChannelFuture channelFuture) {
        CompletableFuture<Channel> future = new CompletableFuture<>();
        channelFuture.addListener((ChannelFutureListener) f -> {
            if (f.isCancelled()) {
                future.cancel(false);
            } else if (f.cause() != null) {
                future.completeExceptionally(f.cause());
            } else {
                future.complete(f.channel());
            }
        });
        return future;
    }
}
