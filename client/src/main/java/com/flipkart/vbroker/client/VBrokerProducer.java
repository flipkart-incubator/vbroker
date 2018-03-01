package com.flipkart.vbroker.client;

import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

@Slf4j
public class VBrokerProducer implements Producer {

    private final VBClientConfig config;
    private final Partitioner partitioner;
    private final Accumulator accumulator;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;

    private Channel channel;

    public VBrokerProducer(VBClientConfig config,
                           Partitioner partitioner,
                           Accumulator accumulator) {
        this.partitioner = partitioner;
        this.accumulator = accumulator;

        this.config = config;
        log.info("Configs: ", config);
        group = new NioEventLoopGroup();
        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);
        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new VBrokerClientInitializer(responseHandlerFactory));
    }

    public VBrokerProducer(VBClientConfig config) {
        this(config, new DefaultPartitioner(), new Accumulator(config));
    }

    @Override
    public CompletionStage<MessageMetadata> produce(ProducerRecord producerRecord) {
        ProducerRecord record = ProducerRecord.builder()
            .messageId("msg_123")
            .groupId("group_123")
            .build();

        accumulator.addRecord(record);

        assert false;
        return getResponse(record);
    }

    private CompletionStage<MessageMetadata> getResponse(ProducerRecord record) {
        //TODO: fill this up correctly
        return CompletableFuture.supplyAsync(() ->
            new MessageMetadata(record.getTopicId(), record.getPartitionId(), new Random().nextInt()));
    }

    @Override
    public void close() {
        if (nonNull(channel)) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                log.error("Error in closing the channel", e);
            }
        }
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
