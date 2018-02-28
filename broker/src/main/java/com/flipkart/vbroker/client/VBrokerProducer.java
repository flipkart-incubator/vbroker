package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.subscribers.DummyEntities;
import com.google.common.collect.Lists;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class VBrokerProducer implements Producer {

    private final VBrokerConfig config;
    private final Partitioner partitioner;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;

    private Channel channel;

    public VBrokerProducer(VBrokerConfig config,
                           Partitioner partitioner) {
        this.config = config;
        this.partitioner = partitioner;
        log.info("Configs: ", config);

        group = new NioEventLoopGroup();

        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);
        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new VBrokerClientInitializer(responseHandlerFactory));
    }

    @Override
    public CompletionStage<MessageMetadata> produce(ProducerRecord producerRecord) throws InterruptedException {

        ProducerRecord record = ProducerRecord.builder()
            .messageId("msg_123")
            .groupId("group_123")
            .build();

        FlatBufferBuilder builder = new FlatBufferBuilder();
        int messageOffset = RecordUtils.flatBuffMsgOffset(builder, record);

        TopicPartition topicPartition = partitioner.partition(record);


        Topic topic = DummyEntities.groupedTopic;
        List<Topic> topics = Lists.newArrayList(topic);
        topics.add(topic);

        int[] topicRequests = new int[topics.size()];
        for (int tIdx = 0; tIdx < topics.size(); tIdx++) {
            Topic currTopic = topics.get(tIdx);

            List<TopicPartition> partitions = Lists.newArrayList(topicPartition);

            int[] partitionRequests = new int[partitions.size()];
            for (int i = 0; i < partitions.size(); i++) {
                int noOfMessages = 1;

                int[] messages = new int[noOfMessages];
                for (int m = 0; m < noOfMessages; m++) {
                    messages[m] = MessageStore.getSampleMsg(builder);
                }

                int messagesVector = MessageSet.createMessagesVector(builder, messages);
                int messageSet = MessageSet.createMessageSet(builder, messagesVector);
                int topicPartitionProduceRequest = TopicPartitionProduceRequest.createTopicPartitionProduceRequest(
                    builder,
                    topicPartition.getId(),
                    (short) 1,
                    messageSet);
                partitionRequests[i] = topicPartitionProduceRequest;
            }

            int partitionRequestsVector = TopicProduceRequest.createPartitionRequestsVector(builder, partitionRequests);
            int topicProduceRequest = TopicProduceRequest.createTopicProduceRequest(builder, currTopic.id(), partitionRequestsVector);
            topicRequests[tIdx] = topicProduceRequest;
        }

        int topicRequestsVector = ProduceRequest.createTopicRequestsVector(builder, topicRequests);
        int produceRequest = ProduceRequest.createProduceRequest(builder, topicRequestsVector);
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            1001,
            RequestMessage.ProduceRequest,
            produceRequest);
        builder.finish(vRequest);
        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);

        Request request = new Request(byteBuf.readableBytes(), byteBuf);

        if (isNull(channel)) {
            ChannelFuture channelFuture = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort());
            //TODO: making this blocking for now, fix this later when we have the accumulator
            channel = channelFuture.sync().channel();
        }

        ChannelFuture channelFuture = channel.writeAndFlush(request);
        CompletableFuture<Channel> future = convert(channelFuture);
        return future
            .thenApply(channel1 ->
                new MessageMetadata(topic.id(), topic.partitions(), new Random().nextInt()));
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
