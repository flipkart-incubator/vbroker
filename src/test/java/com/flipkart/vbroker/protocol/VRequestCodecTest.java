package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.entities.*;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

/**
 * Created by kaushal.hooda on 09/01/18.
 */
public class VRequestCodecTest {
    @BeforeMethod
    public void setUp() {

    }

    private ByteBuffer getSampleProduceRequestAsFlatbuf() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int[] messages = new int[1];
        messages[0] = MessageStore.getSampleMsg(builder);
        int messagesVector = MessageSet.createMessagesVector(builder, messages);
        int messageSet = MessageSet.createMessageSet(builder, messagesVector);
        int produceRequest = ProduceRequest.createProduceRequest(builder,
                (short) 11,
                (short) 1,
                (short) 1,
                messageSet);
        int vRequest = VRequest.createVRequest(builder,
                (byte) 1,
                1001,
                RequestMessage.ProduceRequest,
                produceRequest);
        builder.finish(vRequest);
        return builder.dataBuffer();
    }

    @Test
    public void shouldEncodeDecodeProduceRequest() throws InterruptedException {
        EmbeddedChannel decoder = new EmbeddedChannel(new VRequestDecoder());
        EmbeddedChannel encoder = new EmbeddedChannel(new VRequestEncoder());

        ByteBuffer byteBuffer = getSampleProduceRequestAsFlatbuf();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        Request request = new Request(byteBuf.readableBytes(), byteBuf);

        //Encode the request
        encoder.writeAndFlush(request);
        Object out = encoder.readOutbound();

        //Decode the request
        decoder.writeInbound(out);
        Object decoded = decoder.readInbound();

        //Verify
        ProduceRequest produceRequest = (ProduceRequest) ((VRequest) decoded).requestMessage(new ProduceRequest());
        Message message = produceRequest.messageSet().messages(0);
        Assert.assertEquals(message.messageId(), "msg-1001");
    }
}
