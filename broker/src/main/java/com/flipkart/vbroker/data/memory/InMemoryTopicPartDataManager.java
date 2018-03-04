package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryTopicPartDataManager implements TopicPartDataManager {

    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();

    protected synchronized CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition) {
        return CompletableFuture.supplyAsync(() -> {
            allPartitionsDataMap.computeIfAbsent(topicPartition, topicPartition1 -> {
                TopicPartData topicPartData;
                if (topicPartition1.isGrouped()) {
                    topicPartData = new InMemoryGroupedTopicPartData();
                } else {
                    topicPartData = new InMemoryUnGroupedTopicPartData();
                }
                log.info("TopicPartData: {} for TopicPartition: {}", topicPartData, topicPartition1);
                return topicPartData;
            });
            //allPartitionsDataMap.putIfAbsent(topicPartition, new InMemoryGroupedTopicPartData());
            return allPartitionsDataMap.get(topicPartition);
        });
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(TopicPartition topicPartition, Message message) {
        return getTopicPartData(topicPartition)
            .thenCompose(topicPartData -> topicPartData.addMessage(message));
    }

    @Override
    public CompletionStage<List<MessageMetadata>> addMessages(List<TopicPartMessage> topicPartMessages) {
        return CompletableFuture.supplyAsync(() -> topicPartMessages.stream()
            .map(topicPartMessage -> getTopicPartData(topicPartMessage.getTopicPartition())
                .thenCompose(topicPartData -> topicPartData.addMessage(topicPartMessage.getMessage()))
                .toCompletableFuture().join() //TODO: fix this
            ).collect(Collectors.toList()));
    }

    @Override
    public CompletionStage<MessageMetadata> addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup) {
        throw new NotImplementedException("adding messageGroup to topicPartition is not yet implemented");
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups(TopicPartition topicPartition) {
        return getTopicPartData(topicPartition)
            .thenCompose(TopicPartData::getUniqueGroups);
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom) {
        //TODO: change this to make it non blocking
        return getTopicPartData(topicPartition)
            .thenApplyAsync(topicPartData -> topicPartData.iteratorFrom(group, seqNoFrom))
            .toCompletableFuture().join();
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition, String group) {
        return getTopicPartData(topicPartition).thenCompose((topicPartitionData) -> topicPartitionData.getCurrentOffset(group));
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, int seqNoFrom) {
        return getTopicPartData(topicPartition)
            .thenApplyAsync(topicPartData -> topicPartData.iteratorFrom(seqNoFrom))
            .toCompletableFuture().join();
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(TopicPartition topicPartition) {
        return getTopicPartData(topicPartition).thenCompose(data -> data.getCurrentOffset());
    }
}
