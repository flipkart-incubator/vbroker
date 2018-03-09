package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.iterators.VIterator;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;

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
                MessageGroup messageGroup = new MessageGroup(group, topicPartition);
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
                //subscriberGroupsMap.put(group, subscriberGroup);
                //subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
                subPartDataManager.addGroup(partSubscription, subscriberGroup).toCompletableFuture().join();
            });
        }).toCompletableFuture().join();
    }

    @Override
    public VIterator<IterableMessage> iterator(QType qType) {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<VIterator<IterableMessage>> nextIterator() {
                log.debug("Getting next iterator for QType {}", qType);
                VIterator<IterableMessage> iterator = subPartDataManager.getIterator(partSubscription, qType);
                log.debug("Next iterator: {}", iterator);
                return Optional.of(iterator);
            }
        };
    }
}
