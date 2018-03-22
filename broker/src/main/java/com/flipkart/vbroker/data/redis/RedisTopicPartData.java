package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.HttpHeader;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RedisTopicPartData {

    public DataIterator<Message> iteratorFrom(RList rList, String groupId, int seqNoFrom) {
        return new DataIterator<Message>() {
            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public String name() {
                return "Iterator_redis_grouped_" + groupId + "_at_index_" + seqNoFrom;
            }

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

    ByteBuffer buildMessage(Message message) {
        FlatBufferBuilder builder = FlatbufUtils.newBuilder();
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
