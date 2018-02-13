package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SubscriberDaemon implements Runnable {

    private final LocalAddress address;
    private final Bootstrap consumerBootstrap;
    private final SubscriptionService subscriptionService;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public SubscriberDaemon(LocalAddress address,
                            Bootstrap consumerBootstrap,
                            SubscriptionService subscriptionService) {
        this.address = address;
        this.consumerBootstrap = consumerBootstrap;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void run() {
        this.running.set(true);
        log.info("PartSubscriber now running");

        while (running.get()) {
            try {
                long timeMs = 1000;
                log.info("Sleeping for {} milli secs before connecting to server", timeMs);
                Thread.sleep(timeMs);

                log.info("SubscriberDaemon connecting to server at address {}", address);
                Channel consumerChannel = consumerBootstrap.connect(address).sync().channel();
                log.info("SubscriberDaemon connected to local server address {}", address);

                long pollTimeMs = 5000;
                while (running.get()) {
                    Thread.sleep(pollTimeMs);

                    FlatBufferBuilder builder = new FlatBufferBuilder();

                    Set<Subscription> subscriptionSet = subscriptionService.getAllSubscriptions();
                    List<Subscription> subscriptions = new ArrayList<>(subscriptionSet);
                    short reqNoOfMessages = 1;

                    int[] topicFetchRequests = new int[1];
                    for (int s = 0; s < subscriptions.size(); s++) {
                        Subscription subscription = subscriptions.get(s);
                        Topic topic = subscription.getTopic();
                        List<PartSubscription> partSubscriptions = subscription.getPartSubscriptions();

                        int[] tpFetchRequests = new int[partSubscriptions.size()];
                        for (int ps = 0; ps < partSubscriptions.size(); ps++) {
                            int topicPartitionFetchRequest = TopicPartitionFetchRequest.createTopicPartitionFetchRequest(
                                builder,
                                partSubscriptions.get(ps).getId(),
                                reqNoOfMessages);
                            tpFetchRequests[ps] = topicPartitionFetchRequest;
                        }

                        int partitionRequestsVector = TopicFetchRequest.createPartitionRequestsVector(builder, tpFetchRequests);
                        int topicFetchRequest = TopicFetchRequest.createTopicFetchRequest(builder,
                            subscription.getId(),
                            topic.getId(),
                            partitionRequestsVector);
                        topicFetchRequests[s] = topicFetchRequest;
                    }

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
        log.info("== PartSubscriber shutdown ==");
    }

    public void stop() {
        this.running.set(false);
    }
}
