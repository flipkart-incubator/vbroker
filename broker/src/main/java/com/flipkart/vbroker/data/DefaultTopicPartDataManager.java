package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.utils.CompletionStageUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public abstract class DefaultTopicPartDataManager implements TopicPartDataManager {

    protected abstract CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition);

    @Override
    public CompletionStage<MessageMetadata> addMessage(TopicPartition topicPartition, Message message) {
        //TODO check whether this broker is in fact the leader?
        return getTopicPartData(topicPartition)
            .thenCompose(topicPartData -> topicPartData.addMessage(message));
    }

    @Override
    public CompletionStage<List<MessageMetadata>> addMessages(List<TopicPartMessage> topicPartMessages) {
        List<CompletionStage<MessageMetadata>> stages = topicPartMessages.stream()
            .map(topicPartMessage -> getTopicPartData(topicPartMessage.getTopicPartition())
                .thenCompose(topicPartData -> topicPartData.addMessage(topicPartMessage.getMessage())))
            .collect(Collectors.toList());
        return CompletionStageUtils.listOfStagesToStageOfList(stages);
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups(TopicPartition topicPartition) {
        return getTopicPartData(topicPartition)
            .thenCompose(TopicPartData::getUniqueGroups);
    }

    @Override
    public DataIterator<Message> getIterator(TopicPartition topicPartition, String group) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public DataIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom) {
        TopicPartData topicPartData = getTopicPartData(topicPartition).toCompletableFuture().join();
        return topicPartData.iteratorFrom(group, seqNoFrom);
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition, String group) {
        return getTopicPartData(topicPartition)
            .thenCompose((topicPartitionData) -> topicPartitionData.getCurrentOffset(group));
    }

    @Override
    public DataIterator<Message> getIterator(TopicPartition topicPartition, int seqNoFrom) {
        TopicPartData topicPartData = getTopicPartData(topicPartition).toCompletableFuture().join();
        return topicPartData.iteratorFrom(seqNoFrom);
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition) {
        return getTopicPartData(topicPartition)
            .thenCompose(TopicPartData::getCurrentOffset);
    }
}
