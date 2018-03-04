package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.HttpHeader;
import com.flipkart.vbroker.flatbuf.Message;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.List;
import java.util.stream.Collectors;

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
            record.getCallbackHttpMethod().idx(),
            headersVector,
            payload.length,
            builder.createByteVector(payload)
        );
    }
}
