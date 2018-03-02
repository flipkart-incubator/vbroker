package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.wrappers.Topic;
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
}
