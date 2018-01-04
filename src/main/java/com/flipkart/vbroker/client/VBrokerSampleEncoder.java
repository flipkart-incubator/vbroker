package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.Message;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class VBrokerSampleEncoder {

    public static void main(String args[]) {
        ByteBuffer byteBuffer = encodeSampleMsg();
        //byte[] bytes = builder.sizedByteArray();
        log.info("Message bytes encoded with flatbuffers: {}", byteBuffer);

        Message decodedMsg = Message.getRootAsMessage(byteBuffer);

        decodedMsg.messageId();

        ByteBuffer payloadByteBuf = decodedMsg.bodyPayloadAsByteBuffer();
        int payloadLength = decodedMsg.bodyPayloadLength();

        byte[] bytes = new byte[1024];
        ByteBuffer payloadBuf = payloadByteBuf.asReadOnlyBuffer().get(bytes, 0, payloadLength);
        log.info("Decoded msg with msgId: {} and payload: {};", decodedMsg.messageId(), new String(bytes));
    }

    public static ByteBuffer encodeSampleMsg() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int messageId = builder.createString("msg-1001");
        int groupId = builder.createString("group-1001");
        byte crc = '1';
        byte version = '1';
        short seqNo = 1;
        short topicId = 101;

        byte[] payload = String.valueOf("This is a Varadhi Message").getBytes();

        int sampleMsg = Message.createMessage(
                builder,
                messageId,
                groupId,
                crc,
                version,
                seqNo,
                topicId,
                201,
                payload.length,
                builder.createByteVector(payload)
        );

        builder.finish(sampleMsg);

        return builder.dataBuffer();
    }
}
