package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageConstants;
import com.flipkart.vbroker.utils.ByteBufUtils;
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

    public static Message getRandomMsg(String groupId) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        ByteBuffer sampleByteBuffer = getSampleByteBuffer();
        int sampleMsg = getSampleMsg(builder,
            builder.createString(UUID.randomUUID().toString()),
            builder.createString(groupId),
            sampleByteBuffer);
        builder.finish(sampleMsg);

        return Message.getRootAsMessage(builder.dataBuffer());
    }


    public static int getSampleMsg(FlatBufferBuilder builder) {
        String msgId = UUID.randomUUID().toString();
        int messageId = builder.createString(msgId);
        int groupId = builder.createString(msgId);

        ByteBuffer byteBuffer = getSampleByteBuffer();
        return getSampleMsg(builder, messageId, groupId, byteBuffer);
    }

    private static ByteBuffer getSampleByteBuffer() {
        byte[] bytes = "{\"text\": \"hello\", \"id\": 131}".getBytes();
        return ByteBuffer.wrap(bytes);
    }

    public static int getSampleMsg(FlatBufferBuilder builder, int messageId, int groupId, ByteBuffer byteBuffer) {
        byte crc = '1';
        byte version = '1';
        short seqNo = 1;
        short topicId = 101;
        short partitionId = 0;

        int httpUri = builder.createString("http://localhost:12000/messages");
        byte httpMethod = HttpMethod.POST;
        short callbackTopicId = topicId;
        int callbackHttpUri = httpUri; //builder.createString("http://localhost:12000/messages");
        byte callbackHttpMethod = HttpMethod.POST;

        int httpHeader = HttpHeader.createHttpHeader(builder,
            builder.createString(MessageConstants.APP_ID_HEADER),
            builder.createString("pass-vbroker"));

        int[] headers = new int[1];
        headers[0] = httpHeader;
        int headersVector = Message.createHeadersVector(builder, headers);

        byte[] payload = ByteBufUtils.getBytes(byteBuffer);
        //byte[] payload = byteBuffer.array();
        //byte[] payload = "{\"text\": \"hello\", \"id\": 131}".getBytes();

        return Message.createMessage(
            builder,
            messageId,
            groupId,
            crc,
            version,
            seqNo,
            topicId,
            partitionId,
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
