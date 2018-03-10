package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.MsgIterator;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface TopicPartData {

    public CompletionStage<MessageMetadata> addMessage(Message message);

    public CompletionStage<Set<String>> getUniqueGroups();

    public MsgIterator<Message> iteratorFrom(String group, int seqNoFrom);

    public CompletionStage<Integer> getCurrentOffset(String group);

    public MsgIterator<Message> iteratorFrom(int seqNoFrom);

    public CompletionStage<Integer> getCurrentOffset();
}
