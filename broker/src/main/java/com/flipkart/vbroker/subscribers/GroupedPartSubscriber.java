package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.iterators.MsgIterators;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Slf4j
@EqualsAndHashCode(exclude = {"topicPartDataManager", "subPartDataManager"})
@ToString
public class GroupedPartSubscriber implements PartSubscriber {

    @Getter
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    private final SubPartDataManager subPartDataManager;

    public GroupedPartSubscriber(TopicPartDataManager topicPartDataManager,
                                 SubPartDataManager subPartDataManager,
                                 PartSubscription partSubscription) {
        this.topicPartDataManager = topicPartDataManager;
        this.subPartDataManager = subPartDataManager;
        log.trace("Creating new GroupedPartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
    }

    /**
     * Call this method to keep subscriberGroups in sync with messageGroups at any point
     * Note: This is blocking code internally and it is not part of the subscriber loop
     */
    @Override
    public void refreshSubscriberMetadata() {
        log.debug("Refreshing SubscriberGroups for part-subscriber {} for topic-partition {}",
            partSubscription.getId(), partSubscription.getTopicPartition().getId());
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        topicPartDataManager.getUniqueGroups(topicPartition).thenAccept(uniqueMsgGroups -> {
            //Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();
            Set<String> uniqueSubscriberGroups = subPartDataManager.getUniqueGroups(partSubscription).toCompletableFuture().join();
            Sets.SetView<String> difference = Sets.difference(uniqueMsgGroups, uniqueSubscriberGroups);

            difference.forEach(group -> {
                log.info("Adding diff group {} for topic-partition {}", group, topicPartition);
                MessageGroup messageGroup = new MessageGroup(group, topicPartition);
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
                //subscriberGroupsMap.put(group, subscriberGroup);
                //subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
                subPartDataManager.addGroup(partSubscription, subscriberGroup).toCompletableFuture().join();
            });
        }).toCompletableFuture().join();
    }

    @Override
    public PartSubscriberIterator<IterableMessage> iterator(QType qType) {
        DataIterator<IterableMessage> dataIterator = subPartDataManager.getIterator(partSubscription, qType);
        return MsgIterators.partSubscriberIterator(dataIterator);
    }

    @Override
    public List<IterableMessage> poll(QType qType, int maxRecords, long pollTimeMs) {
        return subPartDataManager.poll(partSubscription, qType, maxRecords, pollTimeMs);
    }

    @Override
    public CompletionStage<Void> commitOffset(String group, int offset) {
        return subPartDataManager.commitOffset(partSubscription, group, offset);
    }

    @Override
    public CompletionStage<Integer> getOffset(String group) {
        return subPartDataManager.getOffset(partSubscription, group);
    }
}
