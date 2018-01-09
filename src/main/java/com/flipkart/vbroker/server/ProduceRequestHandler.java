package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.VResponseEncoder;
import com.google.common.base.Charsets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class ProduceRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final ProduceRequest produceRequest;

    @Override
    public void handle() {
        log.info("Getting messageSet for topic {} and partition {}", produceRequest.topicId(), produceRequest.partitionId());
        MessageSet messageSet = produceRequest.messageSet();
        for (int i = 0; i < messageSet.messagesLength(); i++) {
            Message message = messageSet.messages(i);
            ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
            log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                    Charsets.UTF_8.decode(byteBuffer).toString());

            // Prepare the HTTP request.
            FullHttpRequest httpRequest = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1,
                    io.netty.handler.codec.http.HttpMethod.POST,
                    requireNonNull(message.httpUri()),
                    byteBuf);
            httpRequest.headers().set(HttpHeaderNames.HOST, "localhost");
            httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            httpRequest.headers().set("Content-Type", "application/json");
            httpRequest.headers().set(MessageConstants.MESSAGE_ID_HEADER, message.messageId());
            httpRequest.headers().set(MessageConstants.GROUP_ID_HEADER, message.groupId());

            log.info("Making httpRequest to httpUri: {} and httpMethod: {}",
                    message.httpUri(),
                    HttpMethod.name(message.httpMethod()));

            Bootstrap bootstrap = new Bootstrap()
                    .group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new VResponseEncoder());
                            pipeline.addLast(new HttpResponseHandler(ctx));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect("localhost", 12000);
            channelFuture.addListener((ChannelFutureListener) future -> {
                log.info("channel creation to dest client is {}", future.isSuccess());
                if (!future.isSuccess()) {
                    ctx.channel().close();
                }

                Channel channel = future.channel();
                channel.writeAndFlush(httpRequest);
                log.info("Wrote httpRequest");
                log.info("Handler-> Created outboundChannel to {}", channel.localAddress());
            });
        }
    }
}
