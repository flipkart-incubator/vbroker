package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.DummyEntities;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

public class ByteBufUtilsTest {

    @Test
    public void shouldReturnBytes() {

        byte[] array = {1, 2, 3};
        ByteBuffer buffer = ByteBuffer.wrap(array);
        byte[] bytes = ByteBufUtils.getBytes(buffer);
        IntStream.range(0, array.length).forEach(i -> {
            assertEquals(array[i], bytes[i]);
        });
    }

    @Test
    public void shouldConvertRightlyForTopicEntity() {

        Topic topic1 = DummyEntities.groupedTopic;
        byte[] bytes = ByteBufUtils.getBytes(topic1.getByteBuffer());

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Topic topic2 = Topic.getRootAsTopic(buffer);
        assertEquals(topic1.topicName(), topic2.topicName());
        assertEquals(topic1.topicId(), topic2.topicId());

    }
}
