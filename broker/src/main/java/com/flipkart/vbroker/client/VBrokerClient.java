package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.subscribers.DummyEntities;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class VBrokerClient {

    public static void main(String args[]) throws InterruptedException, IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: ", config);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new VBrokerClientInitializer(responseHandlerFactory));

            Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

            FlatBufferBuilder builder = new FlatBufferBuilder();

            List<Topic> topics = new LinkedList<>();
            topics.add(DummyEntities.topic1);

            int[] topicRequests = new int[topics.size()];
            for (int tIdx = 0; tIdx < topics.size(); tIdx++) {
                Topic topic = topics.get(tIdx);
                List<TopicPartition> partitions = topic.getPartitions();
                int[] partitionRequests = new int[partitions.size()];

                for (int i = 0; i < partitions.size(); i++) {
                    TopicPartition topicPartition = partitions.get(i);
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
                int topicProduceRequest = TopicProduceRequest.createTopicProduceRequest(builder, topic.getId(), partitionRequestsVector);
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
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
