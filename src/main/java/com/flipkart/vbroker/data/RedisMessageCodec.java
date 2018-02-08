package com.flipkart.vbroker.data;

import com.flipkart.vbroker.entities.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import io.netty.handler.codec.base64.Base64;

public class RedisMessageCodec implements Codec {

    private final Decoder decoder = new Decoder() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            return Message.getRootAsMessage(Base64.decode(buf).nioBuffer());
        }
    };

    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes(((Message)in).getByteBuffer());
            return Base64.encode(buf);
        }
    };

    @Override
    public Decoder<Object> getMapValueDecoder() {
        return getValueDecoder();
    }

    @Override
    public Encoder getMapValueEncoder() {
        return getValueEncoder();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return getValueDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return getValueEncoder();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
}
