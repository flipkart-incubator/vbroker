package com.flipkart.vbroker.protocol.apis;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.protocol.VRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.ToString;

import java.nio.ByteBuffer;

@ToString
public class ProduceRequest extends VRequest {

    public ProduceRequest(ByteBuffer byteBuffer) {
        super.setApiKey(ApiKey.PRODUCE_REQUEST);
        /*
         * by default for now it's V1.
         * later we can have ProduceRequestV1, ProduceRequestV2 classes.etc.
         */
        super.setVersion((short) 1);
        setRequest(byteBuffer);
    }

    private void setRequest(ByteBuffer byteBuffer) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        super.setRequestLength(byteBuf.readableBytes());
        super.setRequestPayload(byteBuf);
    }

    public Message getMessage() {
        return Message.getRootAsMessage(super.getRequestPayload().nioBuffer());
    }
}
