package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Request;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SubscriberDaemon implements Runnable {

    private final LocalAddress address;
    private final Bootstrap consumerBootstrap;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public SubscriberDaemon(LocalAddress address,
                            Bootstrap consumerBootstrap) {
        this.address = address;
        this.consumerBootstrap = consumerBootstrap;
    }

    @Override
    public void run() {
        this.running.set(true);
        log.info("Subscriber now running");

        while (running.get()) {
            try {
                long timeMs = 1000;
                log.info("Sleeping for {} milli secs before connecting to server", timeMs);
                Thread.sleep(timeMs);

                log.info("Subscriber connecting to server at address {}", address);
                Channel consumerChannel = consumerBootstrap.connect(address).sync().channel();
                log.info("Subscriber connected to local server address {}", address);

                long pollTimeMs = 5000;
                while (running.get()) {
                    Thread.sleep(pollTimeMs);

                    FlatBufferBuilder builder = new FlatBufferBuilder();

                    int[] tpFetchRequests = new int[1];
                    int topicPartitionFetchRequest = TopicPartitionFetchRequest.createTopicPartitionFetchRequest(
                            builder,
                            (short) 1,
                            (short) 1);
                    tpFetchRequests[0] = topicPartitionFetchRequest;

                    int[] topicFetchRequests = new int[1];
                    int partitionRequestsVector = TopicFetchRequest.createPartitionRequestsVector(builder, tpFetchRequests);

                    int topicFetchRequest = TopicFetchRequest.createTopicFetchRequest(builder,
                            (short) 11,
                            partitionRequestsVector);
                    topicFetchRequests[0] = topicFetchRequest;
                    int topicRequestsVector = FetchRequest.createTopicRequestsVector(builder, topicFetchRequests);

                    int fetchRequest = FetchRequest.createFetchRequest(builder, topicRequestsVector);
                    int vRequest = VRequest.createVRequest(builder,
                            (byte) 1,
                            1001,
                            RequestMessage.FetchRequest,
                            fetchRequest);
                    builder.finish(vRequest);
                    ByteBuffer byteBuffer = builder.dataBuffer();
                    ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                    Request request = new Request(byteBuf.readableBytes(), byteBuf);

                    log.info("Sending FetchRequest to broker");
                    consumerChannel.writeAndFlush(request);
                }

                consumerChannel.closeFuture().sync();
                break;
            } catch (InterruptedException e) {
                log.error("Exception in consumer in connecting to the broker", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.info("== Subscriber shutdown ==");
    }

    public void stop() {
        this.running.set(false);
    }
}
