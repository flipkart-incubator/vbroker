package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface TopicPartDataManager {

    public CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition);

    public CompletionStage<MessageMetadata> addMessage(TopicPartition topicPartition, Message message);

    public CompletionStage<List<MessageMetadata>> addMessages(List<TopicPartMessage> topicPartMessages);

    public CompletionStage<MessageMetadata> addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup);

    public CompletionStage<Set<String>> getUniqueGroups(TopicPartition topicPartition);

    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group);

    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom);

    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, int seqNoFrom);
}
