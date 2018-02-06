package com.flipkart.vbroker.core;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

public interface MemoryManager  {

    public Map getMessageGroupMap(short partitionId, short topicId);

    public List getMessageList(String groupId, short partitionId, short topicId);

    public static void setAllocator(Channel ch, ByteBufAllocator allocator) {
        ch.config().setAllocator(allocator);
    }
}
