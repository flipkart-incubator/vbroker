package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class FetchRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        FetchRequest fetchRequest = (FetchRequest) vRequest.requestMessage(new FetchRequest());
        assert nonNull(fetchRequest);

        return CompletableFuture.supplyAsync(() -> {
            FlatBufferBuilder builder = new FlatBufferBuilder();
            int fetchResponse = buildFetchResponse(fetchRequest, builder);
            int vResponse = VResponse.createVResponse(builder,
                vRequest.correlationId(),
                ResponseMessage.FetchResponse,
                fetchResponse);
            builder.finish(vResponse);

            return VResponse.getRootAsVResponse(builder.dataBuffer());
        });
    }

    private int buildFetchResponse(FetchRequest fetchRequest, FlatBufferBuilder builder) {
        int noOfTopics = fetchRequest.topicRequestsLength();
        int[] topicFetchResponses = new int[noOfTopics];

        for (int i = 0; i < noOfTopics; i++) {
            TopicFetchRequest topicFetchRequest = fetchRequest.topicRequests(i);

            Topic topic = topicService.getTopic(topicFetchRequest.topicId()).toCompletableFuture().join();
            Subscription subscription = subscriptionService.getSubscription(topicFetchRequest.topicId(), topicFetchRequest.subscriptionId()).toCompletableFuture().join();

            int noOfPartitionsInFetchReq = topicFetchRequest.partitionRequestsLength();
            log.info("Handling FetchRequest for topic {} and subscription {} with {} partition requests",
                topic.id(), subscription.id(), noOfPartitionsInFetchReq);
            int[] partitionFetchResponses = new int[noOfPartitionsInFetchReq];
            for (int j = 0; j < noOfPartitionsInFetchReq; j++) {
                TopicPartitionFetchRequest topicPartitionFetchRequest = topicFetchRequest.partitionRequests(j);
                int partitionId = topicPartitionFetchRequest.partitionId();

                TopicPartition topicPartition = topicService.getTopicPartition(topic, partitionId).toCompletableFuture().join();
                PartSubscription partSubscription = subscriptionService.getPartSubscription(subscription, topicPartition.getId()).toCompletableFuture().join();

                partitionFetchResponses[j] = buildTopicPartitionFetchResponse(
                    builder,
                    topic,
                    topicPartition,
                    partSubscription,
                    topicPartitionFetchRequest);
            }

            int partitionResponsesVector = TopicFetchResponse.createPartitionResponsesVector(builder, partitionFetchResponses);
            int topicFetchResponse = TopicFetchResponse.createTopicFetchResponse(
                builder,
                subscription.id(),
                topicFetchRequest.topicId(),
                partitionResponsesVector);
            topicFetchResponses[i] = topicFetchResponse;
        }

        int topicResponsesVector = FetchResponse.createTopicResponsesVector(builder, topicFetchResponses);
        return FetchResponse.createFetchResponse(builder, topicResponsesVector);
    }

    private int buildTopicPartitionFetchResponse(FlatBufferBuilder builder,
                                                 Topic topic,
                                                 TopicPartition topicPartition,
                                                 PartSubscription partSubscription,
                                                 TopicPartitionFetchRequest topicPartitionFetchRequest) {
        int noOfMessagesToFetch = topicPartitionFetchRequest.noOfMessages();
        int partitionId = topicPartitionFetchRequest.partitionId();
        QType qType = getQType(topicPartitionFetchRequest.qType());
        log.info("Handling FetchRequest for {} messages for topic {} and partition {} and qType {}",
            noOfMessagesToFetch, topic.id(), partitionId, qType);

        int[] messages = buildMessages(builder,
            partSubscription,
            qType,
            noOfMessagesToFetch,
            topicPartitionFetchRequest.timeOutMs());
        log.debug("Writing {} messages for topic {} and partition {} in FetchResponse",
            messages.length, topicPartition.getId(), partitionId);
        int messagesVector = MessageSet.createMessagesVector(builder, messages);
        int messageSet = MessageSet.createMessageSet(builder, messagesVector);
        int vStatus = VStatus.createVStatus(builder, StatusCode.ConsumeSuccess_NoError, builder.createString(""));

        return TopicPartitionFetchResponse.createTopicPartitionFetchResponse(
            builder,
            partitionId,
            vStatus,
            messageSet);
    }

    private QType getQType(int protoQType) {
        return QType.getQType(protoQType);
    }

    private int[] buildMessages(FlatBufferBuilder builder,
                                PartSubscription partSubscription,
                                QType qType,
                                int noOfMessagesToFetch,
                                int reqPollTimeoutMs) {
        PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription).toCompletableFuture().join();
        int actualPollTimeoutMs = getPollTimeoutMs(reqPollTimeoutMs);
        List<IterableMessage> iterableMessages = partSubscriber.poll(qType, noOfMessagesToFetch, actualPollTimeoutMs);
        log.info("No of polled messages are {}", iterableMessages.size());
        log.info("PartSubscriberOffset for group_0 is {}", partSubscriber.getOffset("group_0").toCompletableFuture().join());
        List<Integer> messagePosList = iterableMessages.stream()
            .map(iterableMessage -> buildMessage(builder, iterableMessage.getMessage()))
            .collect(Collectors.toList());
        return Ints.toArray(messagePosList);
    }

    private int getPollTimeoutMs(int reqPollTimeoutMs) {
        final int maxPollTimeoutMs = 5000;
        int actualPollTimeoutMs = Math.min(maxPollTimeoutMs, reqPollTimeoutMs);
        actualPollTimeoutMs = Math.max(actualPollTimeoutMs, 1); //should be at-least 1ms
        return actualPollTimeoutMs;
    }

    private int buildMessage(FlatBufferBuilder builder, Message message) {
        int headersVector = Message.createHeadersVector(builder, new int[0]);

        ByteBuffer payloadByteBuffer = message.bodyPayloadAsByteBuffer();
        byte[] messageBytes = new byte[payloadByteBuffer.remaining()];
        payloadByteBuffer.get(messageBytes);

        return Message.createMessage(
            builder,
            builder.createString(requireNonNull(message.messageId())),
            builder.createString(requireNonNull(message.groupId())),
            message.crc(),
            message.version(),
            message.seqNo(),
            message.topicId(),
            message.partitionId(),
            201,
            builder.createString(requireNonNull(message.httpUri())),
            message.httpMethod(),
            message.callbackTopicId(),
            builder.createString(requireNonNull(message.callbackHttpUri())),
            message.callbackHttpMethod(),
            headersVector,
            messageBytes.length,
            builder.createByteVector(messageBytes));
    }
}
