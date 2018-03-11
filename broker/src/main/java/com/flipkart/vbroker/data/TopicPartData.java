package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface TopicPartData {

    CompletionStage<MessageMetadata> addMessage(Message message);

    CompletionStage<Set<String>> getUniqueGroups();

    DataIterator<Message> iteratorFrom(String group, int seqNoFrom);

    CompletionStage<Integer> getCurrentOffset(String group);

    DataIterator<Message> iteratorFrom(int seqNoFrom);

    CompletionStage<Integer> getCurrentOffset();
}
