package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RScript;
import org.redisson.api.RSetAsync;
import org.redisson.api.RedissonClient;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class RedisGroupedTopicPartDataLua implements TopicPartData {

    private static RedissonClient client;
    private TopicPartition topicPartition;

    public RedisGroupedTopicPartDataLua(RedissonClient client,
                                        TopicPartition topicPartition) {
        this.client = client;
        this.topicPartition = topicPartition;
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {

        Message messageBuffer = Message.getRootAsMessage(buildMessage(message));
        MessageGroup messageGroup = new MessageGroup(messageBuffer.groupId(), topicPartition);

        RedisObject rObjMessage = new RedisMessageObject(messageBuffer);
        RedisObject rObjString = new RedisStringObject(messageBuffer.groupId());

        return client.getScript().evalAsync(RScript.Mode.READ_WRITE,
            "if ((redis.call('lpush', KEYS[1], ARGV[1])) > 0) then " +
                "return (redis.call('sadd', KEYS[2], ARGV[2]));" +
                "end",
            RScript.ReturnType.INTEGER,
            Arrays.asList("{" + topicPartition.toString() + "}" + messageGroup.toString(), topicPartition.toString()), rObjMessage, rObjString)
            .thenApplyAsync(result -> {
                return new MessageMetadata(message.topicId(), message.partitionId(), new Random().nextInt());
            })
            .exceptionally(exception -> {
                throw new VBrokerException("Unable to add message to redis : " + exception.getMessage());
            });

    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {

        RSetAsync<RedisObject> topicPartitionSet = client.getSet(topicPartition.toString());
        return topicPartitionSet.readAllAsync().thenApplyAsync(result -> {
            if (result != null || result.size() != 0) {
                return result
                    .stream()
                    .map(entry -> ((RedisStringObject) entry).getStringData())
                    .collect(Collectors.toSet());
            } else {
                return null;
            }
        }).exceptionally(exception -> {
                throw new VBrokerException("Unable to get Unique Groups :" + ((Exception) exception).getMessage());
            }
        );
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String groupId, int seqNoFrom) {
        log.info("getting peeking iterator");
        MessageGroup messageGroup = new MessageGroup(groupId, topicPartition);
        RList<RedisObject> rList = client.getList("{" + topicPartition.toString() + "}" + messageGroup.toString());

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

    @Override
    public PeekingIterator<Message> iteratorFrom(int seqNoFrom) {
        throw new UnsupportedOperationException("You cannot have a global iterator for partition for a grouped topic-partition");
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
