package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class ProduceRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final ProducerService producerService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        ProduceRequest produceRequest = (ProduceRequest) vRequest.requestMessage(new ProduceRequest());
        assert nonNull(produceRequest);

        FlatBufferBuilder builder = new FlatBufferBuilder();
        Map<Integer, List<Integer>> topicPartitionResponseMap = new HashMap<>();
        List<TopicPartMessage> messagesToProduce = new LinkedList<>();

        return CompletableFuture.supplyAsync(() -> {
            log.info("Handling ProduceRequest for {} topic requests", produceRequest.topicRequestsLength());
            for (int i = 0; i < produceRequest.topicRequestsLength(); i++) {
                TopicProduceRequest topicProduceRequest = produceRequest.topicRequests(i);
                log.info("Received ProduceRequest for topic {}", topicProduceRequest.topicId());
                Topic topic = topicService.getTopic(topicProduceRequest.topicId()).toCompletableFuture().join();

                for (int j = 0; j < topicProduceRequest.partitionRequestsLength(); j++) {
                    TopicPartitionProduceRequest topicPartitionProduceRequest = topicProduceRequest.partitionRequests(j);
                    TopicPartition topicPartition = topicService
                        .getTopicPartition(topic, topicPartitionProduceRequest.partitionId())
                        .toCompletableFuture().join();

                    MessageSet messageSet = topicPartitionProduceRequest.messageSet();
                    log.info("MessageSet for topic {} and partition {} has {} messages",
                        topic.id(), topicPartitionProduceRequest.partitionId(), messageSet.messagesLength());

                    for (int m = 0; m < messageSet.messagesLength(); m++) {
                        Message message = messageSet.messages(m);
                        messagesToProduce.add(TopicPartMessage.newInstance(topicPartition, message));
                    }
                }
            }

            //below call can block
            producerService.produceMessages(messagesToProduce);

            for (TopicPartMessage topicPartMessage : messagesToProduce) {
                int vStatus = VStatus.createVStatus(builder, StatusCode.ProduceSuccess_NoError, builder.createString(""));
                int topicPartitionProduceResponse = TopicPartitionProduceResponse.createTopicPartitionProduceResponse(
                    builder,
                    topicPartMessage.getTopicPartition().getId(),
                    vStatus);
                topicPartitionResponseMap.computeIfAbsent(topicPartMessage.getTopicPartition().getTopicId(),
                    o -> new LinkedList<>())
                    .add(topicPartitionProduceResponse);
            }

            int noOfTopics = topicPartitionResponseMap.keySet().size();
            int[] topicProduceResponses = new int[noOfTopics];
            int i = 0;
            for (Map.Entry<Integer, List<Integer>> entry : topicPartitionResponseMap.entrySet()) {
                Integer topicId = entry.getKey();
                List<Integer> partitionResponsesList = entry.getValue();
                int[] partitionResponses = Ints.toArray(partitionResponsesList);

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
                vRequest.correlationId(),
                ResponseMessage.ProduceResponse,
                produceResponse);
            builder.finish(vResponse);

            return VResponse.getRootAsVResponse(builder.dataBuffer());
        });
    }
}
