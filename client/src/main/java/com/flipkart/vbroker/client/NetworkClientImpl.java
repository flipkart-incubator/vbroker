package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
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

import static java.util.Objects.nonNull;

@Slf4j
public class NetworkClientImpl implements NetworkClient {

    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;

    private final Map<Node, Channel> nodeChannelMap = new ConcurrentHashMap<>();
    private final Map<VRequest, VResponse> vRequestResponseMap = new ConcurrentHashMap<>();

    private final Map<Integer, VRequest> requestMap = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<VResponse>> responseFutureMap = new ConcurrentHashMap<>();

    public NetworkClientImpl() {
        eventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("network_client_impl"));
        bootstrap = new Bootstrap()
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
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
                        }
                    });
                }
            });
    }

    @Override
    public CompletionStage<VResponse> send(Node node, VRequest vRequest) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(vRequest.getByteBuffer());
        Request request = new Request(byteBuf.readableBytes(), byteBuf);

        CompletableFuture<VResponse> responseFuture = new CompletableFuture<>();
        responseFutureMap.put(vRequest.correlationId(), responseFuture);

        //checkout an existing/new channel
        Channel channel = checkoutChannel(node);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) future1 -> {
            log.info("Finished writing request {} to channel", vRequest.correlationId());
        });

        return responseFuture;
    }

    public Channel checkoutChannel(Node node) {
        nodeChannelMap.computeIfAbsent(node, node1 -> {
            try {
                return bootstrap.connect(node.getHostIp(), node.getHostPort()).sync().channel();
            } catch (InterruptedException e) {
                log.error("Error in creating a new Channel to node {}", node1);
                return null;
            }
        });
        return nodeChannelMap.get(node);
    }

    @Override
    public void close() throws IOException {
        nodeChannelMap.forEach((node, channel) -> {
            if (nonNull(channel)) {
                try {
                    channel.close().get();
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
}
