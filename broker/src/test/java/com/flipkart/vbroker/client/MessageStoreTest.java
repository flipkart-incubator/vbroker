package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.Message;
import com.google.common.io.Files;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.testng.Assert.assertEquals;

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

        assertEquals((short) 101, message_2.topicId());
        assertEquals(message_2.messageId(), message_3.messageId());

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
}