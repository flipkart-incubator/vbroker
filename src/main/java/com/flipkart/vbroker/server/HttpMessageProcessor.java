package com.flipkart.vbroker.server;

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

    private final Bootstrap httpClientBootstrap;

    @Override
    public void process(Message message) {
        log.info("Processing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
        makeHttpRequest(message);
    }

    private void makeHttpRequest(Message message) {
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

        ChannelFuture channelFuture = httpClientBootstrap.connect("localhost", 12000);
        channelFuture.addListener((ChannelFutureListener) future -> {
            log.info("channel creation to dest client is {}", future.isSuccess());
            Channel channel = future.channel();
            if (!future.isSuccess()) {
                channel.close();
            } else {
                log.info("Handler-> Created outboundChannel to {}", channel.localAddress());
                channel.writeAndFlush(httpRequest);
                log.debug("Wrote httpRequest");
            }
        });
    }
}
