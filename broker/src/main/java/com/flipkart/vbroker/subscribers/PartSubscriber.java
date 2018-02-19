package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@EqualsAndHashCode(exclude = {"subscriberGroupsMap", "subscriberGroupIteratorMap"})
@ToString
public class PartSubscriber implements IPartSubscriber {
    public static final Integer DEFAULT_PARALLELISM = 5;
    public static final Integer MAX_GROUPS_IN_ITERATOR = 100;

    @Getter
    private final PartSubscription partSubscription;
    @Getter
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<SubscriberGroup, PeekingIterator<IMessageWithGroup>> subscriberGroupIteratorMap = new LinkedHashMap<>();
    private final TopicPartDataManager topicPartDataManager;

    public PartSubscriber(TopicPartDataManager topicPartDataManager,
                          PartSubscription partSubscription) {
        this.topicPartDataManager = topicPartDataManager;
        log.trace("Creating new PartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
    }

    /**
     * Call this method to keep subscriberGroups in sync with messageGroups at any point
     */
    public void refreshSubscriberMetadata() {
        log.debug("Refreshing SubscriberGroups for part-subscriber {} for topic-partition {}",
            partSubscription.getId(), partSubscription.getTopicPartition().getId());
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        topicPartDataManager.getUniqueGroups(topicPartition).thenAccept(uniqueMsgGroups -> {
            Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();
            Sets.SetView<String> difference = Sets.difference(uniqueMsgGroups, uniqueSubscriberGroups);

            difference.forEach(group -> {
                MessageGroup messageGroup = new MessageGroup(group, topicPartition);
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
                subscriberGroupsMap.put(group, subscriberGroup);
                subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            });
        });
    }

    @Override
    public PeekingIterator<IMessageWithGroup> iterator() {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IMessageWithGroup>> nextIterator() {
                if (log.isDebugEnabled()) {
                    List<String> groupIds = subscriberGroupsMap.values().stream()
                        .map(SubscriberGroup::getGroupId)
                        .collect(Collectors.toList());
                    log.debug("SubscriberGroupsMap values: {}", Collections.singletonList(groupIds));
                }

                return subscriberGroupsMap.values()
                    .stream()
                    .filter(group -> !group.isLocked())
                    .filter(subscriberGroupIteratorMap::containsKey)
                    .map(subscriberGroupIteratorMap::get)
                    .filter(Iterator::hasNext)
                    .findFirst();
            }
        };
    }
}
