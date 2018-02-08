package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
public class RedisTopicPartData implements TopicPartData {

    private static Config messageCodecConfig = new Config();
    private static Config defaultCodecConfig = new Config();
    private static RedissonClient messageCodecClient;
    private static RedissonClient defaultCodecClient;
    private TopicPartition topicPartition;
    private static Codec redisCodec=new RedisMessageCodec();

    static {
        messageCodecConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
        defaultCodecConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
        messageCodecConfig.setCodec(redisCodec);
        messageCodecClient = Redisson.create(messageCodecConfig);
        defaultCodecClient = Redisson.create(defaultCodecConfig);
    }

    public RedisTopicPartData(TopicPartition topicPartition) {
        this();
        this.topicPartition = topicPartition;
    }

    public RedisTopicPartData() {

    }

    @Override
    public void addMessage(Message message) {
        Message messageBuffer = Message.getRootAsMessage(buildMessage(message));
        MessageGroup messageGroup = new MessageGroup(messageBuffer.groupId(),topicPartition);
        RList<Message> rList = messageCodecClient.getList(messageGroup.toString());
        RList<String> stringRList = defaultCodecClient.getList(topicPartition.toString());
        stringRList.add(messageBuffer.groupId());
        rList.add(messageBuffer);
    }

    @Override
    public Set<String> getUniqueGroups() {
        RList<String> stringRList = defaultCodecClient.getList(topicPartition.toString());
        if (stringRList.size() != 0) {
            return new HashSet<String>(stringRList.subList(0, stringRList.size()));
        }
        else return new HashSet<>();
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String groupId, int seqNoFrom) {
        log.info("getting peeking iterator");
        MessageGroup messageGroup = new MessageGroup(groupId,topicPartition);
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
