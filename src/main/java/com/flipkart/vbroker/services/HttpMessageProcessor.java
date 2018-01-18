package com.flipkart.vbroker.services;

import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class HttpMessageProcessor implements MessageProcessor {

    private final Bootstrap clientBootstrap;

    @Override
    public void process(Message message) {
        // Prepare the HTTP request.
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                io.netty.handler.codec.http.HttpMethod.POST,
                requireNonNull(message.httpUri()),
                Unpooled.wrappedBuffer(message.bodyPayloadAsByteBuffer()));
        httpRequest.headers().set(HttpHeaderNames.HOST, "localhost");
        httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        httpRequest.headers().set("Content-Type", "application/json");
        httpRequest.headers().set(MessageConstants.MESSAGE_ID_HEADER, message.messageId());
        httpRequest.headers().set(MessageConstants.GROUP_ID_HEADER, message.groupId());

        log.info("Making httpRequest to httpUri: {} and httpMethod: {}",
                message.httpUri(),
                HttpMethod.name(message.httpMethod()));

//        Bootstrap bootstrap = new Bootstrap()
//                .group(ctx.channel().eventLoop())
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<Channel>() {
//                    @Override
//                    protected void initChannel(Channel ch) {
//                        ChannelPipeline pipeline = ch.pipeline();
//                        pipeline.addLast(new HttpClientCodec());
//                        pipeline.addLast(new VResponseEncoder());
//                        pipeline.addLast(new HttpResponseHandler(ctx));
//                    }
//                });

        ChannelFuture channelFuture = clientBootstrap.connect("localhost", 12000);
        channelFuture.addListener((ChannelFutureListener) future -> {
            log.info("channel creation to dest client is {}", future.isSuccess());
            if (!future.isSuccess()) {
                //ctx.channel().close();
            }

            Channel channel = future.channel();
            channel.writeAndFlush(httpRequest);
            log.info("Wrote httpRequest");
            log.info("Handler-> Created outboundChannel to {}", channel.localAddress());
        });

    }
}
