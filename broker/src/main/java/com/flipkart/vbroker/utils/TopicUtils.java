package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopicUtils {

    public static List<TopicPartition> getTopicPartitions(Topic topic) {
        return IntStream.range(0, topic.partitions())
            .mapToObj(i -> new TopicPartition((short) i, topic.id(), topic.grouped()))
            .collect(Collectors.toList());
    }

    public static Topic getTopic(byte[] bytes) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        return Topic.getRootAsTopic(byteBuf.nioBuffer());
    }
}
