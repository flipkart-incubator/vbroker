package com.flipkart.vbroker.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.exceptions.BrokerUnAvailableException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.utils.MetricUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class NetworkClientImpl implements NetworkClient {

    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;

    private final Map<Node, Channel> nodeChannelMap = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<VResponse>> responseFutureMap = new ConcurrentHashMap<>();
    private final Histogram requestBytesHistogram;

    public NetworkClientImpl(MetricRegistry metricRegistry) {
        eventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("network_client_impl"));
        bootstrap = new Bootstrap()
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ResponseChannelInitializer());
        this.requestBytesHistogram = metricRegistry.histogram(MetricUtils.clientFullMetricName("request.bytes"));
    }

    @Override
    public CompletionStage<VResponse> send(Node node, VRequest vRequest) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(vRequest.getByteBuffer());
        Request request = new Request(byteBuf.readableBytes(), byteBuf);
        //update the no of bytes metric
        requestBytesHistogram.update(request.getRequestLength());

        CompletableFuture<VResponse> responseFuture = new CompletableFuture<>();
        responseFutureMap.put(vRequest.correlationId(), responseFuture);

        //checkout an existing/new channel
        Channel channel = checkoutChannel(node);
        if (isNull(channel)) {
            VBrokerException ex = new BrokerUnAvailableException("BrokerNode: " + node + " is not available to connect");
            responseFuture.completeExceptionally(ex);
        } else {
            channel.writeAndFlush(request).addListener((ChannelFutureListener) future1 -> {
                log.info("Finished writing request {} to channel", vRequest.correlationId());
            });
        }

        return responseFuture;
    }

    /**
     * this method blocks to checkout a channel to a node
     * blocking primarily as we want to connect to the node to do some work currently
     * later we can change it to ignore this node if others are available,
     * especially because Sender thread blocks on one node otherwise
     * Currently this gives up after 3 retries
     *
     * @param node to checkout channel to
     * @return the established channel or null if failure
     */
    private Channel checkoutChannel(Node node) {
        //TODO: handle the case when channel is not ready.etc.
        nodeChannelMap.computeIfAbsent(node, node1 -> {
            Channel channel = null;
            int retries = 1;
            while (retries <= 3) {
                try {
                    ChannelFuture nodeChannelFuture = bootstrap.connect(node.getHostIp(), node.getHostPort());
                    channel = nodeChannelFuture.sync().channel();
                    log.info("Established a new channel to node {}", node);
                    break;
                } catch (Exception e) {
                    int retryTime = 1000 * retries;
                    log.error("Error in creating a new Channel to node {}. Sleeping for {}ms before possible retry",
                        node1, e, retryTime);
                    try {
                        Thread.sleep(1000 * retries);
                    } catch (InterruptedException ignored) {
                    }

                    retries++;
                }
            }
            return channel;
        });
        return nodeChannelMap.get(node);
    }

    @Override
    public void close() throws IOException {
        log.info("Closing down NetworkClient");
        nodeChannelMap.forEach((node, channel) -> {
            if (nonNull(channel)) {
                try {
                    //if (channel.isActive()) {
                    channel.close().get();
                    //}
                    log.info("Closed channel to node {}", node);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error in closing channel", e);
                }
            }
        });

        try {
            eventLoopGroup.shutdownGracefully().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception in shutting down event loop group", e);
            throw new IOException(e);
        }
    }

    private class ResponseChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new VBrokerClientCodec());
            pipeline.addLast(new SimpleChannelInboundHandler<VResponse>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, VResponse vResponse) {
                    log.info("Received VResponse with correlationId {}", vResponse.correlationId());
                    responseFutureMap.forEach((key, value) ->
                        log.info("ResponseFutureMap entries: ({}, {})", key, value));
                    CompletableFuture<VResponse> respFuture = responseFutureMap.get(vResponse.correlationId());
                    log.info("Completing future {}", respFuture);
                    try {
                        respFuture.complete(vResponse);
                    } catch (Throwable ex) {
                        log.error("Error in completing future {}", ex.getMessage());
                        respFuture.completeExceptionally(ex);
                    }

                    log.info("RespFuture completion status {}", respFuture);
                    responseFutureMap.remove(vResponse.correlationId());
                }
            });
        }
    }
}
