package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RListAsync;
import org.redisson.api.RedissonClient;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RedisUnGroupedTopicPartData implements TopicPartData {
    private static RedissonClient client;
    private TopicPartition topicPartition;

    public RedisUnGroupedTopicPartData(RedissonClient client,
                                       TopicPartition topicPartition) {
        this.client = client;
        this.topicPartition = topicPartition;

    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {
        Message messageBuffer = Message.getRootAsMessage(buildMessage(message));
        RedisObject rObjMessage = new RedisMessageObject(messageBuffer);
        RListAsync<RedisObject> topicPartitionList = client.getList(topicPartition.toString());
        RFuture<Boolean> topicPartitionAddFuture = topicPartitionList.addAsync(rObjMessage);
        return topicPartitionAddFuture.thenApplyAsync(result -> {
            if (result) {
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
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot list unique groups");
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot have a group level iterator");
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(int seqNoFrom) {
        log.info("getting peeking iterator");
        RList<RedisObject> rList = client.getList(topicPartition.toString());

        return new PeekingIterator<Message>() {
            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public boolean hasNext() {
                return index.get() < rList.size();
            }

            @Override
            public Message peek() {
                return ((RedisMessageObject) rList.get(index.get())).getMessage();
            }

            @Override
            public Message next() {
                return ((RedisMessageObject) rList.get(index.getAndIncrement())).getMessage();
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }

        };
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
