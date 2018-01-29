package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageConstants;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.UUID;

@Slf4j
public class MessageStore {

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
        int sampleMsg = getSampleMsg(builder);
        builder.finish(sampleMsg);
        return builder.dataBuffer();
    }

    public static int getSampleMsg(FlatBufferBuilder builder) {
        String msgId = UUID.randomUUID().toString();
        int messageId = builder.createString(msgId);
        int groupId = builder.createString(msgId);
        byte crc = '1';
        byte version = '1';
        short seqNo = 1;
        short topicId = 101;

        int httpUri = builder.createString("http://localhost:12000/messages");
        byte httpMethod = HttpMethod.POST;
        short callbackTopicId = 101;
        int callbackHttpUri = builder.createString("http://localhost:12000/messages");
        byte callbackHttpMethod = HttpMethod.POST;

        int httpHeader = HttpHeader.createHttpHeader(builder,
                builder.createString(MessageConstants.APP_ID_HEADER),
                builder.createString("pass-vbroker"));

        int[] headers = new int[1];
        headers[0] = httpHeader;
        int headersVector = Message.createHeadersVector(builder, headers);
        byte[] payload = "{\"text\": \"hello\", \"id\": 131}".getBytes();

        return Message.createMessage(
                builder,
                messageId,
                groupId,
                crc,
                version,
                seqNo,
                topicId,
                201,
                httpUri,
                httpMethod,
                callbackTopicId,
                callbackHttpUri,
                callbackHttpMethod,
                headersVector,
                payload.length,
                builder.createByteVector(payload)
        );
    }
}
