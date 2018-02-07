package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroupMap;
import com.flipkart.vbroker.core.MessageList;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Observer;

@Slf4j
public class LocalMemoryManager implements MemoryManager, Observer {

    public static void setAllocator(Channel ch, ByteBufAllocator allocator) {
        ch.config().setAllocator(allocator);
    }

    @Override
    public Map getMessageGroupMap(short partitionId, short topicId) {
        return MessageGroupMap.getInstance(partitionId, topicId);
    }

    @Override
    public List getMessageList(String groupId, short partitionId, short topicId) {
        MessageList messageList = MessageList.getInstance(groupId, partitionId, topicId);
        messageList.addObserver(this);
        return messageList;
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        log.info("MessageList with groupId {} updated with capacity {}", (((MessageList.ObservableVList) o).getGroupId()), arg);
    }

}
