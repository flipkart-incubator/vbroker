package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RListAsync;
import org.redisson.api.RedissonClient;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Slf4j
public class RedisTopicPartData implements TopicPartData {

    private static RedissonClient messageCodecClient;
    private static RedissonClient defaultCodecClient;
    private TopicPartition topicPartition;

    public RedisTopicPartData(RedissonClient messageCodecClient,
                              RedissonClient defaultCodecClient,
                              TopicPartition topicPartition) {
        this.messageCodecClient = messageCodecClient;
        this.defaultCodecClient = defaultCodecClient;
        this.topicPartition = topicPartition;
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {

        Message messageBuffer = Message.getRootAsMessage(buildMessage(message));
        MessageGroup messageGroup = new MessageGroup(messageBuffer.groupId(), topicPartition);

        RListAsync<Message> messageGroupList = messageCodecClient.getList(messageGroup.toString());
        RListAsync<String> topicPartitionList = defaultCodecClient.getList(topicPartition.toString());

        RFuture<Boolean> topicPartitionAddFuture = topicPartitionList.addAsync(messageBuffer.groupId());
        RFuture<Boolean> messageGroupAddFuture = messageGroupList.addAsync(messageBuffer);

        return topicPartitionAddFuture.thenCombineAsync(messageGroupAddFuture, (topicPartitionResult, messageGroupResult) -> {
            if (topicPartitionResult && messageGroupResult) {
                return new MessageMetadata(message.topicId(), message.partitionId(), new Random().nextInt());
            } else {
                throw new VBrokerException("Unable to add message to redis : adding to topicPartitionList or messageGroupList failed");
            }
        }).exceptionally(exception -> {
            throw new VBrokerException("Unable to add message to redis : " + exception.getMessage());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        RListAsync<String> topicPartitionList = defaultCodecClient.getList(topicPartition.toString());

        RFuture<Integer> topicPartitionListSizeFuture = topicPartitionList.sizeAsync();
        RFuture<List<String>> topicPartitionReadFuture = topicPartitionList.readAllAsync();

        return topicPartitionListSizeFuture.thenCombineAsync(topicPartitionReadFuture, (sizeResult, readResult) -> {
            if (sizeResult > 0 && readResult.size() > 0) {
                return ((Set<String>) new HashSet<>(readResult));
            } else if (sizeResult == 0 && readResult.size() == 0) {
                return null;
            } else {
                throw new VBrokerException("Unable to get Unique Groups : inconsistent results from topicPartitionList");
            }
        }).exceptionally(exception -> {
            throw new VBrokerException("Unable to get Unique Groups : " + exception.getMessage());
        });
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String groupId, int seqNoFrom) {
        log.info("getting peeking iterator");
        MessageGroup messageGroup = new MessageGroup(groupId, topicPartition);
        RList<Message> rList = messageCodecClient.getList(messageGroup.toString());
        return Iterators.peekingIterator(rList.listIterator(seqNoFrom));
    }

    private ByteBuffer buildMessage(Message message) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int httpHeader = HttpHeader.createHttpHeader(builder,
            builder.createString(message.headers(0).key()),
            builder.createString(message.headers(0).value()));

        int[] headers = new int[1];
        headers[0] = httpHeader;
        int headersVector = Message.createHeadersVector(builder, headers);
        byte[] arr = new byte[message.bodyPayloadAsByteBuffer().remaining()];
        message.bodyPayloadAsByteBuffer().get(arr);
        int i = Message.createMessage(builder,
            builder.createString(message.messageId()),
            builder.createString(message.groupId()),
            message.crc(),
            message.version(),
            message.seqNo(),
            message.topicId(),
            message.partitionId(),
            message.attributes(),
            builder.createString(message.httpUri()),
            message.httpMethod(),
            message.callbackTopicId(),
            builder.createString(message.callbackHttpUri()),
            message.callbackHttpMethod(),
            headersVector,
            arr.length,
            builder.createByteVector(arr)
        );
        builder.finish(i);
        return builder.dataBuffer();
    }
}
