package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class ProduceRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final ProducerService producerService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public ListenableFuture<VResponse> handle(VRequest vRequest) {
        ProduceRequest produceRequest = (ProduceRequest) vRequest.requestMessage(new ProduceRequest());
        assert nonNull(produceRequest);

        return listeningExecutorService.submit(() -> {
            FlatBufferBuilder builder = new FlatBufferBuilder();
            Map<Short, List<Integer>> topicPartitionResponseMap = new HashMap<>();

            for (int i = 0; i < produceRequest.topicRequestsLength(); i++) {
                TopicProduceRequest topicProduceRequest = produceRequest.topicRequests(i);
                Topic topic = topicService.getTopic(topicProduceRequest.topicId());

                for (int j = 0; j < topicProduceRequest.partitionRequestsLength(); j++) {
                    TopicPartitionProduceRequest topicPartitionProduceRequest = topicProduceRequest.partitionRequests(j);
                    log.info("Getting messageSet for topic {} and partition {}", topicProduceRequest.topicId(), topicPartitionProduceRequest.partitionId());
                    TopicPartition topicPartition = topicService.getTopicPartition(topic, topicPartitionProduceRequest.partitionId());

                    MessageSet messageSet = topicPartitionProduceRequest.messageSet();
                    for (int m = 0; m < messageSet.messagesLength(); m++) {
                        Message message = messageSet.messages(m);
                        ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
                        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                        log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                                Charsets.UTF_8.decode(byteBuffer).toString());

                        producerService.produceMessage(topicPartition, message);

                        int vStatus = VStatus.createVStatus(builder, StatusCode.ProduceSuccess_NoError, builder.createString(""));
                        int topicPartitionProduceResponse = TopicPartitionProduceResponse.createTopicPartitionProduceResponse(
                                builder,
                                topicPartition.getId(),
                                vStatus);
                        topicPartitionResponseMap.computeIfAbsent(topicPartition.getId(),
                                o -> new LinkedList<>())
                                .add(topicPartitionProduceResponse);
                    }
                }
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
                        topicId,
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

            return VResponse.getRootAsVResponse(builder.dataBuffer());
        });
    }
}
