package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.flatbuf.MessageSet;
import com.google.common.io.Files;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MessageStoreTest {

    @Test
    public void shouldEncodeAndDecodeReturnSameMsg() throws Exception {
        FlatBufferBuilder builder_1 = new FlatBufferBuilder();
        builder_1.finish(MessageStore.getSampleMsg(builder_1));
        ByteBuffer byteBuffer = builder_1.dataBuffer();

        File file = new File("/tmp/flatbuff.txt");
        write(byteBuffer.asReadOnlyBuffer(), file);

        File file_2 = new File("/tmp/flatbuff_2.txt");
        write(byteBuffer.asReadOnlyBuffer(), file_2);

        Message message_2 = getMessage(file);
        Message message_3 = getMessage(file_2);


        //builder_1.dataBuffer()

        assertEquals((short) 101, message_2.topicId());
        assertEquals(message_2.messageId(), message_3.messageId());

        Message.getRootAsMessage(MessageStore.encodeSampleMsg());
        Message msg_1 = Message.getRootAsMessage(MessageStore.encodeSampleMsg());
        Message msg_2 = Message.getRootAsMessage(MessageStore.encodeSampleMsg());
        assertEquals(msg_1.topicId(), msg_2.topicId());
    }

    private Message getMessage(File file) throws IOException {
        byte[] readBytes = Files.toByteArray(file);
        ByteBuffer byteBuffer_2 = Unpooled.copiedBuffer(readBytes).nioBuffer();
        return Message.getRootAsMessage(byteBuffer_2);
    }

    private void write(ByteBuffer byteBuffer, File file) throws IOException {
        try (FileChannel fileChannel = new FileOutputStream(file, false).getChannel()) {
            fileChannel.write(byteBuffer.asReadOnlyBuffer());
        }
    }

    @Test
    public void shouldReEncodeFinishedFlatBuff_IntoNewFlatBuff() {
        FlatBufferBuilder builder_1 = new FlatBufferBuilder();
        builder_1.finish(MessageStore.getSampleMsg(builder_1));
        ByteBuffer byteBuffer = builder_1.dataBuffer();
        Message message_1 = Message.getRootAsMessage(byteBuffer);

        FlatBufferBuilder builder_2 = new FlatBufferBuilder();
        String msgId = UUID.randomUUID().toString();
        int messageId = builder_2.createString(msgId);
        int groupId = builder_2.createString(msgId);

        int sampleMsg = MessageStore.getSampleMsg(builder_2, messageId, groupId, byteBuffer);
        builder_2.finish(sampleMsg);

        Message message_2 = Message.getRootAsMessage(builder_2.dataBuffer());
        //assertEquals(message_2.bodyPayloadLength(), );
        Message message_1Replica = Message.getRootAsMessage(message_2.bodyPayloadAsByteBuffer());
        assertNotNull(message_1Replica);
        assertEquals(message_1Replica.messageId(), message_1.messageId());
    }

    @Test
    public void shouldConstructMessageSet_FromExistingBuiltFlatBuffMessage() {
        FlatBufferBuilder builder_1 = new FlatBufferBuilder();
        builder_1.finish(MessageStore.getSampleMsg(builder_1));
        ByteBuffer byteBuffer_1 = builder_1.dataBuffer();
        Message message_1 = Message.getRootAsMessage(byteBuffer_1);

        FlatBufferBuilder builder_2 = new FlatBufferBuilder();

        int[] messageOffsets = new int[1];
        //messageOffsets[0] = builder_2.createByteVector(ByteBufUtils.getBytes(byteBuffer_1));
        //messageOffsets[0] = Message.createMessage()
        int messagesVector = MessageSet.createMessagesVector(builder_2, messageOffsets);
        int messageSetOffset = MessageSet.createMessageSet(builder_2, messagesVector);
        builder_2.finish(messageSetOffset);

        MessageSet messageSet = MessageSet.getRootAsMessageSet(builder_2.dataBuffer());
        assertEquals(messageSet.messagesLength(), 1);
        assertEquals(messageSet.messages(0).messageId(), message_1.messageId());
    }
}