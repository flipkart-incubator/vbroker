package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface TopicPartDataManager {

    CompletionStage<MessageMetadata> addMessage(TopicPartition topicPartition, Message message);

    CompletionStage<List<MessageMetadata>> addMessages(List<TopicPartMessage> topicPartMessages);

    CompletionStage<Set<String>> getUniqueGroups(TopicPartition topicPartition);

    DataIterator<Message> getIterator(TopicPartition topicPartition, String group);

    DataIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom);

    DataIterator<Message> getIterator(TopicPartition topicPartition, int seqNoFrom);

    CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition, String group);

    CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition);
}
