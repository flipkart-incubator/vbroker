package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.entities.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class RedisMessageCodec implements Codec {

    private final Charset charset = CharsetUtil.UTF_8;

    private final Decoder decoder = new Decoder() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            RedisObject redisObj = null;
            try {
                buf = Base64.decode(buf);
                int objType = buf.readInt();
                if (objType == RedisObject.ObjectType.MESSAGE.ordinal()) {
                    redisObj = new RedisMessageObject(Message.getRootAsMessage(buf.nioBuffer()));
                } else if (objType == RedisObject.ObjectType.STRING.ordinal()) {
                    String str = buf.toString(charset);
                    buf.readerIndex(buf.readableBytes());
                    redisObj = new RedisStringObject(str);
                }
            } finally {
                buf.release();
            }
            return redisObj;
        }
    };

    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
            switch (((RedisObject) in).getObjectType()) {
                case MESSAGE:
                    buf.writeInt((RedisObject.ObjectType.MESSAGE).ordinal());
                    buf.writeBytes(((RedisMessageObject) in).getMessage().getByteBuffer());
                    break;
                case STRING:
                    buf.writeInt((RedisObject.ObjectType.STRING).ordinal());
                    buf.writeCharSequence(((RedisStringObject) in).getStringData().toString(), charset);
                    break;
            }
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
