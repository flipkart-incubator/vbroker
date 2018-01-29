package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.*;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.collect.PeekingIterator;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class FetchRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final SubscriptionService subscriptionService;

    @Override
    public VResponse handle(VRequest vRequest) {
        FetchRequest fetchRequest = (FetchRequest) vRequest.requestMessage(new FetchRequest());
        assert !Objects.isNull(fetchRequest);

        FlatBufferBuilder builder = new FlatBufferBuilder();
        int fetchResponse = buildFetchResponse(fetchRequest, builder);
        int vResponse = VResponse.createVResponse(builder,
                1001,
                ResponseMessage.FetchResponse,
                fetchResponse);
        builder.finish(vResponse);

        return VResponse.getRootAsVResponse(builder.dataBuffer());
    }

    private int buildFetchResponse(FetchRequest fetchRequest, FlatBufferBuilder builder) {
        int noOfTopics = fetchRequest.topicRequestsLength();
        int[] topicFetchResponses = new int[noOfTopics];

        for (int i = 0; i < noOfTopics; i++) {
            TopicFetchRequest topicFetchRequest = fetchRequest.topicRequests(i);
            Topic topic = topicService.getTopic(topicFetchRequest.topicId());
            Subscription subscription = subscriptionService.getSubscription(topicFetchRequest.subscriptionId());
            int noOfPartitionsInFetchReq = topicFetchRequest.partitionRequestsLength();
            log.info("Handling FetchRequest for topic {} and subscription {} with {} partition requests",
                    topic.getId(), subscription.getId(), noOfPartitionsInFetchReq);
            int[] partitionFetchResponses = new int[noOfPartitionsInFetchReq];
            for (int j = 0; j < noOfPartitionsInFetchReq; j++) {
                TopicPartitionFetchRequest topicPartitionFetchRequest = topicFetchRequest.partitionRequests(j);
                short partitionId = topicPartitionFetchRequest.partitionId();
                TopicPartition topicPartition = topicService.getTopicPartition(topic, partitionId);
                PartSubscription partSubscription = subscription.getPartSubscription(topicPartition.getId());

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
                    subscription.getId(),
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
        short noOfMessagesToFetch = topicPartitionFetchRequest.noOfMessages();
        short partitionId = topicPartitionFetchRequest.partitionId();
        log.info("Handling FetchRequest for {} messages for topic {} and partition {}",
                noOfMessagesToFetch, topic.getId(), partitionId);

        int[] messages = buildMessages(builder, partSubscription, noOfMessagesToFetch);
        log.info("Writing {} messages for topic {} and partition {} in FetchResponse",
                messages.length, topicPartition.getId(), partitionId);
        int messagesVector = MessageSet.createMessagesVector(builder, messages);
        int messageSet = MessageSet.createMessageSet(builder, messagesVector);

        return TopicPartitionFetchResponse.createTopicPartitionFetchResponse(
                builder,
                partitionId,
                (short) 200,
                messageSet);
    }

    private int[] buildMessages(FlatBufferBuilder builder,
                                PartSubscription partSubscription,
                                short noOfMessagesToFetch) {
        PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription);
        PeekingIterator<SubscriberGroup> groupIterator = partSubscriber.iterator();
        List<Integer> messages = new ArrayList<>();

        int m = 0;
        while (groupIterator.hasNext() && m < noOfMessagesToFetch) {
            SubscriberGroup group = groupIterator.peek();
            log.info("Fetching messages of group {} for topic {}", group.getGroupId(), partSubscription.getTopicPartition().getId());

            PeekingIterator<Message> messageIterator = group.iterator();
            while (messageIterator.hasNext()) {
                Message msg = messageIterator.peek();
                log.debug("Peeking Message with msg_id: {} and group_id: {}", msg.messageId(), msg.groupId());
                messages.add(buildMessage(builder, msg));
                m++;
                messageIterator.next();
            }
            groupIterator.next();
        }

        return Ints.toArray(messages);
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
