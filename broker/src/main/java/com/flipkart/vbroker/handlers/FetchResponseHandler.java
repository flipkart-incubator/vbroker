package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.entities.HttpMethod;
import com.google.common.base.Charsets;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class FetchResponseHandler implements ResponseHandler {

    private final Bootstrap clientBootstrap;

    @Override
    public void handle(VResponse vResponse) {
        FetchResponse fetchResponse = (FetchResponse) vResponse.responseMessage(new FetchResponse());
        assert fetchResponse != null;

        for (int i = 0; i < fetchResponse.topicResponsesLength(); i++) {
            TopicFetchResponse topicFetchResponse = fetchResponse.topicResponses(i);
            for (int j = 0; j < topicFetchResponse.partitionResponsesLength(); j++) {
                TopicPartitionFetchResponse topicPartitionFetchResponse = topicFetchResponse.partitionResponses(j);
                VStatus status = topicPartitionFetchResponse.status();
                log.info("FetchResponse status for topic {} and partition {} is {}",
                        topicFetchResponse.topicId(), topicPartitionFetchResponse.partitionId(), status.statusCode());

                if (StatusCode.ConsumeSuccess_NoError == status.statusCode()) {
                    MessageSet messageSet = topicPartitionFetchResponse.messageSet();
                    int noOfMessages = messageSet.messagesLength();
                    log.info("Handling FetchResponse for topic {} and partition {} having {} messages",
                            topicFetchResponse.topicId(), topicPartitionFetchResponse.partitionId(), noOfMessages);
                    for (int m = 0; m < noOfMessages; m++) {
                        Message message = messageSet.messages(m);
                        ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
                        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                        log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                                Charsets.UTF_8.decode(byteBuffer).toString());
                        makeHttpRequest(message);
                    }
                }
            }
        }
    }

    public void makeHttpRequest(Message message) {
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

        ChannelFuture channelFuture = clientBootstrap.connect("localhost", 12000);
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
