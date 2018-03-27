package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        FlatBufferBuilder builder = FlatbufUtils.newBuilder();
        Map<Integer, List<CompletableFuture<Integer>>> topicResponseMap = new HashMap<>();
        log.info("Handling ProduceRequest for {} topic requests", produceRequest.topicRequestsLength());

        for (int i = 0; i < produceRequest.topicRequestsLength(); i++) {
            TopicProduceRequest topicProduceRequest = produceRequest.topicRequests(i);
            log.info("Received ProduceRequest for topic {} having {} partition requests",
                topicProduceRequest.topicId(), topicProduceRequest.partitionRequestsLength());
            Topic topic = topicService.getTopic(topicProduceRequest.topicId()).toCompletableFuture().join();
            List<CompletableFuture<Integer>> topicPartFutureList = Lists.newArrayList();

            for (int j = 0; j < topicProduceRequest.partitionRequestsLength(); j++) {
                TopicPartitionProduceRequest topicPartitionProduceRequest = topicProduceRequest.partitionRequests(j);
                TopicPartition topicPartition = topicService
                    .getTopicPartition(topic, topicPartitionProduceRequest.partitionId())
                    .toCompletableFuture().join();

                MessageSet messageSet = topicPartitionProduceRequest.messageSet();
                log.info("MessageSet for topic {} and partition {} has {} messages",
                    topic.id(), topicPartitionProduceRequest.partitionId(), messageSet.messagesLength());

                List<Message> messages = IntStream.range(0, messageSet.messagesLength())
                    .mapToObj(messageSet::messages)
                    .collect(Collectors.toList());

                CompletionStage<List<MessageMetadata>> stage = producerService.produceMessages(topicPartition, messages);
                CompletionStage<Integer> topicPartStage = stage.thenApply(messageMetadataList -> {
                    log.info("Done producing {} messages to topic-partition {}", messages.size(), topicPartition);
                    int vStatus = VStatus.createVStatus(builder, StatusCode.ProduceSuccess_NoError, builder.createString(""));
                    return TopicPartitionProduceResponse.createTopicPartitionProduceResponse(
                        builder,
                        topicPartition.getId(),
                        vStatus);
                });

                topicPartFutureList.add(topicPartStage.toCompletableFuture());
            }
            topicResponseMap.put(topic.id(), topicPartFutureList);
        }

        List<CompletableFuture<Integer>> topicProduceResponseFutureList = Lists.newArrayList();
        topicResponseMap.forEach((topicId, futureList) -> {
            log.info("Topic {} has {} partFutures", topicId, futureList.size());
            CompletableFuture[] topicPartFuturesArr = futureList.toArray(new CompletableFuture[futureList.size()]);

            CompletableFuture<Integer> topicProduceResponseFuture = CompletableFuture.allOf(topicPartFuturesArr)
                .thenApply(aVoid -> {
                    List<Integer> partitionResponseList = futureList
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                    int[] partitionResponses = Ints.toArray(partitionResponseList);
                    int partitionResponsesVector = TopicProduceResponse.createPartitionResponsesVector(builder, partitionResponses);
                    return TopicProduceResponse.createTopicProduceResponse(
                        builder,
                        topicId,
                        partitionResponsesVector);
                });
            topicProduceResponseFutureList.add(topicProduceResponseFuture);
        });

        CompletableFuture[] topicProduceResponseFutures = topicProduceResponseFutureList.toArray(new CompletableFuture[topicProduceResponseFutureList.size()]);
        log.info("TopicProduceResponse futures are {}", topicProduceResponseFutures.length);

        return CompletableFuture.allOf(topicProduceResponseFutures)
            .thenApply(aVoid -> {
                List<Integer> topicResponses = topicProduceResponseFutureList.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                int[] topicProduceResponses = Ints.toArray(topicResponses);
                log.info("TopicResponses are {}", topicProduceResponses.length);

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
