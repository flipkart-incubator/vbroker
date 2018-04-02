package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface TopicPartData extends AutoCloseable {

    CompletionStage<MessageMetadata> addMessage(Message message);

    CompletionStage<Set<String>> getUniqueGroups();

    DataIterator<Message> iteratorFrom(String group, int seqNoFrom); //TODO: name this unGroupedIteratorFrom(...)

    CompletionStage<Integer> getCurrentOffset(String group);

    DataIterator<Message> iteratorFrom(int seqNoFrom);//TODO: name this groupedIteratorFrom(...)

    CompletionStage<Integer> getCurrentOffset();

    @Override
    default void close() throws Exception {
        //do nothing
    }
}
