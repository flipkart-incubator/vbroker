package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Response;
import com.flipkart.vbroker.services.ProducerService;
import com.google.common.base.Charsets;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class ProduceRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final ProduceRequest produceRequest;
    private final ProducerService producerService;

    @Override
    public void handle() {
        log.info("Getting messageSet for topic {} and partition {}", produceRequest.topicId(), produceRequest.partitionId());
        MessageSet messageSet = produceRequest.messageSet();

        FlatBufferBuilder builder = new FlatBufferBuilder();
        Map<Short, List<Integer>> topicPartitionResponseMap = new HashMap<>();

        for (int i = 0; i < messageSet.messagesLength(); i++) {
            Message message = messageSet.messages(i);
            ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
            log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                    Charsets.UTF_8.decode(byteBuffer).toString());

            producerService.produceMessage(message);

            produceRequest.topicId();
            int topicPartitionProduceResponse = TopicPartitionProduceResponse.createTopicPartitionProduceResponse(
                    builder,
                    produceRequest.partitionId(),
                    (short) 200);

            topicPartitionResponseMap.computeIfAbsent(produceRequest.topicId(),
                    o -> new LinkedList<>())
                    .add(topicPartitionProduceResponse);
        }

        int noOfTopics = topicPartitionResponseMap.keySet().size();
        int[] topicProduceResponses = new int[noOfTopics];
        int i = 0;
        for (Map.Entry<Short, List<Integer>> entry : topicPartitionResponseMap.entrySet()) {
            Short topicId = entry.getKey();
            List<Integer> partitionResponsesList = entry.getValue();
            int[] partitionResponses = new int[partitionResponsesList.size()];
            for (int j = 0; j < partitionResponses.length; j++) {
                partitionResponses[j] = partitionResponsesList.get(j);
            }

            int partitionResponsesVector = TopicProduceResponse.createPartitionResponsesVector(builder, partitionResponses);
            int topicProduceResponse = TopicProduceResponse.createTopicProduceResponse(
                    builder,
                    produceRequest.topicId(),
                    partitionResponsesVector);
            topicProduceResponses[i++] = topicProduceResponse;
        }

        int topicResponsesVector = ProduceResponse.createTopicResponsesVector(builder, topicProduceResponses);
        int produceResponse = ProduceResponse.createProduceResponse(builder, topicResponsesVector);
        int vResponse = VResponse.createVResponse(builder,
                1001,
                ResponseMessage.ProduceResponse,
                produceResponse);
        builder.finish(vResponse);
        ByteBuf responseByteBuf = Unpooled.wrappedBuffer(builder.dataBuffer());

        Response response = new Response(responseByteBuf.readableBytes(), responseByteBuf);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
