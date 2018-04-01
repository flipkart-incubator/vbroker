package com.flipkart.vbroker.client;

import com.flipkart.vbroker.exceptions.InvalidMessageException;
import com.flipkart.vbroker.flatbuf.HttpHeader;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.ByteBufUtils;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class RecordUtils {

    public static int flatBuffMsgOffset(FlatBufferBuilder builder,
                                        ProducerRecord record,
                                        int partitionId) {
        List<Integer> collect = record.getHeaders()
            .entrySet()
            .stream()
            .map(entry -> HttpHeader.createHttpHeader(builder,
                builder.createString(entry.getKey()),
                builder.createString(entry.getValue())))
            .collect(Collectors.toList());
        int[] headersOffset = Ints.toArray(collect);
        int headersVector = Message.createHeadersVector(builder, headersOffset);
        byte[] payload = record.getPayload();

        if (Strings.isNullOrEmpty(record.getMessageId())) {
            throw new InvalidMessageException("MessageId cannot be empty/null");
        }

        if (Strings.isNullOrEmpty(record.getGroupId())) {
            throw new InvalidMessageException("GroupId cannot be empty/null");
        }

        //TODO: do remaining validations for uri, method.etc.

        ProducerRecord.HttpMethod callbackHttpMethod = record.getCallbackHttpMethod();
        byte callbackHttpMethodByte = 0;
        if (nonNull(callbackHttpMethod)) {
            callbackHttpMethodByte = callbackHttpMethod.idx();
        }

        return Message.createMessage(
            builder,
            builder.createString(record.getMessageId()),
            builder.createString(record.getGroupId()),
            record.getCrc(),
            record.getVersion(),
            record.getSeqNo(),
            record.getTopicId(),
            partitionId,
            record.getAttributes(),
            builder.createString(record.getHttpUri()),
            record.getHttpMethod().idx(),
            record.getCallbackTopicId(),
            builder.createString(record.getCallbackHttpUri()),
            callbackHttpMethodByte,
            headersVector,
            payload.length,
            builder.createByteVector(payload)
        );
    }

    public static ConsumerRecord newConsumerRecord(Message message) {
        return ConsumerRecord.builder()
            .messageId(message.messageId())
            .groupId(message.groupId())
            .crc(message.crc())
            .version(message.version())
            .seqNo(message.seqNo())
            .topicId(message.topicId())
            .attributes(message.attributes())
            .payload(message.bodyPayloadAsByteBuffer())
            .build();
    }

    public static ProducerRecord newProducerRecord(Message message) {
        return ProducerRecord.builder()
            .groupId(message.groupId())
            .messageId(message.messageId())
            .crc((byte) 1)
            .version((byte) 1)
            .seqNo(1)
            .topicId(message.topicId())
            .attributes(201)
            .httpUri(message.httpUri())
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(message.callbackTopicId())
            .callbackHttpUri(message.callbackHttpUri())
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(ByteBufUtils.getBytes(message.bodyPayloadAsByteBuffer()))
            .build();
    }
}
