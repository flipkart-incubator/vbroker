package com.flipkart.vbroker.server;

import com.flipkart.vbroker.controller.TopicService;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Response;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@AllArgsConstructor
public class TopicCreateRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final TopicCreateRequest topicCreateRequest;
    private final TopicService topicService;

    @Override
    public void handle() {
        Topic topic = new Topic(topicCreateRequest.id(), topicCreateRequest.partitions(),
                topicCreateRequest.replicationFactor(), topicCreateRequest.grouped(), topicCreateRequest.topicName(),
                topicCreateRequest.team(), com.flipkart.vbroker.core.Topic.TopicCategory
                .valueOf(TopicCategory.name(topicCreateRequest.topicCategory())));
        topicService.createTopic(topic);

        FlatBufferBuilder topicResponseBuilder = new FlatBufferBuilder();
        int topicCreateResponse = TopicCreateResponse.createTopicCreateResponse(topicResponseBuilder, topic.getId(),
                (short) 200);
        int topicVResponse = VResponse.createVResponse(topicResponseBuilder, 1002, RequestMessage.TopicCreateRequest,
                topicCreateResponse);
        topicResponseBuilder.finish(topicVResponse);
        ByteBuffer topicResponseBuffer = topicResponseBuilder.dataBuffer();
        ByteBuf res = Unpooled.wrappedBuffer(topicResponseBuffer);

        Response topResponse = new Response(res.readableBytes(), res);
        ctx.write(topResponse).addListener(ChannelFutureListener.CLOSE);

    }

}
