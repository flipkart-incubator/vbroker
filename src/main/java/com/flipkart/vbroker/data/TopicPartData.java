package com.flipkart.vbroker.data;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;

import java.util.Set;

public interface TopicPartData {

    public void addMessage(Message message);

    public Set<String> getUniqueGroups();

    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom);
}
