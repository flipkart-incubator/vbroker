package com.flipkart.vbroker.core;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

public class MemoryManager {

    /*
    public Message allocateMessage(ChannelHandlerContext ctx, byte[] bytes);

    public void freeMessage(Message message);

    public MessageGroup createNewMessageGroup();

    public void addMessage(Message message, MessageGroup messageGroup);

    //used for upgrade/downgrade
    public void setLevel(MessageGroup messageGroup, MessageGroup.Level level);
    */

    public static Map getCapacityManagedMap(EvictionStrategy evictionStrategy, L3Provider l3Provider) {
        return new CapacityManagedMap(evictionStrategy, l3Provider);
    }

    public static List getCapacityManagedList(String groupId) {
        return new CapacityManagedList(groupId);
    }

    public static void setAllocator(Channel ch, ByteBufAllocator allocator) {
        ch.config().setAllocator(allocator);
    }
}
