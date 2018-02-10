package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.data.RedisMessageCodec;
import com.flipkart.vbroker.entities.Message;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static com.flipkart.vbroker.client.MessageStore.encodeSampleMsg;

public class MessageCodecTest {

    private static Config redisCodecConfig = new Config();
    private static Config stringConfig = new Config();

    private static RedissonClient redisCodecClient;
    private static RedissonClient stringClient;
    private static Codec redisCodec = new RedisMessageCodec();
    private static Codec fstCodec = new JsonJacksonCodec();

    static {
        redisCodecConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
        stringConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisCodecConfig.setCodec(redisCodec);
        redisCodecClient = Redisson.create(redisCodecConfig);
        stringClient = Redisson.create(stringConfig);
    }

    private short topicPartition;

    @BeforeMethod
    public void setUp() {

    }

    @Test
    private void shouldEncodeDecodeMessageTest() {
        ByteBuffer byteBuffer = encodeSampleMsg();
        Message decodedMsg = Message.getRootAsMessage(byteBuffer);
        RList<Message> rList = redisCodecClient.getList("test");
        rList.add(decodedMsg);
        int s = ((Message) (redisCodecClient.getList("test").get(0))).topicId();
        Assert.assertEquals(s, 101);

    }
}
