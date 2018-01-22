package com.flipkart.vbroker.ioengine;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import io.netty.buffer.ByteBuf;

import java.util.List;

public interface IOEngine {

    public Message createMessage(ByteBuf byteBuf);

    public Message updateMessage(Message message);

    public void addMessage(Message message, MessageGroup messageGroup);

    public void addMessage(Message message, TopicPartition topicPartition);

    public List<Message> getMessages(TopicPartition topicPartition);

    public List<Message> getUnconsumedMessages(TopicPartition topicPartition);
}
