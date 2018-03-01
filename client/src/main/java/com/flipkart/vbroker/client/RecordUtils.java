package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.Message;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class RecordUtils {

    public static int flatBuffMsgOffset(FlatBufferBuilder fbuilder, ProducerRecord record) {
        List<Integer> collect = record.getHeaders()
            .entrySet()
            .stream()
            .map(entry -> HttpHeader.createHttpHeader(fbuilder,
                fbuilder.createString(entry.getKey()),
                fbuilder.createString(entry.getValue())))
            .collect(Collectors.toList());
        int[] headersOffset = Ints.toArray(collect);
        int headersVector = Message.createHeadersVector(fbuilder, headersOffset);
        byte[] payload = record.getPayload();

        return Message.createMessage(
            fbuilder,
            fbuilder.createString(record.getMessageId()),
            fbuilder.createString(record.getGroupId()),
            record.getCrc(),
            record.getVersion(),
            record.getSeqNo(),
            record.getTopicId(),
            record.getPartitionId(),
            record.getAttributes(),
            fbuilder.createString(record.getHttpUri()),
            record.getHttpMethod().idx(),
            record.getCallbackTopicId(),
            fbuilder.createString(record.getCallbackHttpUri()),
            record.getCallbackHttpMethod().idx(),
            headersVector,
            payload.length,
            fbuilder.createByteVector(payload)
        );
    }
}
