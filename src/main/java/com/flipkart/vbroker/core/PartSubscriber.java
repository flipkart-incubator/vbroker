package com.flipkart.vbroker.core;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@EqualsAndHashCode(exclude = {"subscriberGroupsMap", "subscriberGroupIteratorMap"})
@ToString
public class PartSubscriber implements Iterable<MessageWithGroup> {
    public static final Integer DEFAULT_PARALLELISM = 5;
    public static final Integer MAX_GROUPS_IN_ITERATOR = 100;

    @Getter
    private final PartSubscription partSubscription;
    @Getter
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<SubscriberGroup, PeekingIterator<MessageWithGroup>> subscriberGroupIteratorMap = new LinkedHashMap<>();
    private final TopicPartitionDataManager topicPartitionDataManager;

    public PartSubscriber(TopicPartitionDataManager topicPartitionDataManager,
                          PartSubscription partSubscription) {
        this.topicPartitionDataManager = topicPartitionDataManager;
        log.trace("Creating new PartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
        //refreshSubscriberGroups();
    }

    public boolean lockSubscriberGroup(String groupId) {
        Optional<SubscriberGroup> subscriberGroup = Optional.ofNullable(subscriberGroupsMap.get(groupId));
        if (subscriberGroup.isPresent()) {
            return subscriberGroup.get().lock();
        }
        throw new VBrokerException("SubscriberGroup with groupId: " + groupId + " not present");
    }

    public boolean unlockSubscriberGroup(String groupId) {
        Optional<SubscriberGroup> subscriberGroup = Optional.ofNullable(subscriberGroupsMap.get(groupId));
        if (!subscriberGroup.isPresent()) {
            throw new VBrokerException("SubscriberGroup with groupId: " + groupId + " not present");
        }
        return subscriberGroup.get().unlock();
    }

    /**
     * Call this method to keep subscriberGroups in sync with messageGroups at any point
     */
    public void refreshSubscriberGroups() {
        log.debug("Refreshing SubscriberGroups for part-subscriber {} for topic-partition {}",
                partSubscription.getId(), partSubscription.getTopicPartition().getId());
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        Set<String> uniqueMsgGroups = topicPartitionDataManager.getUniqueGroups(topicPartition);
        Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();

        Sets.SetView<String> difference = Sets.difference(uniqueMsgGroups, uniqueSubscriberGroups);
        for (String group : difference) {
            Optional<MessageGroup> messageGroup = topicPartitionDataManager.getMessageGroup(topicPartition, group);
            if (messageGroup.isPresent()) {
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup.get(), topicPartition);
                subscriberGroupsMap.put(group, subscriberGroup);
                subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            }
        }
    }

    public PeekingIterator<SubscriberGroup> groupIterator() {
        return Iterators.peekingIterator(subscriberGroupsMap.values().iterator());
    }

    @Override
    public PeekingIterator<MessageWithGroup> iterator() {
        return new PeekingIterator<MessageWithGroup>() {
            PeekingIterator<MessageWithGroup> currIterator;

            boolean refresh() {
                boolean refreshed = false;
                if (currIterator != null
                        && currIterator.hasNext()
                        && currIterator.peek().isGroupLocked()) {
                    return true;
                }

                for (SubscriberGroup subscriberGroup : subscriberGroupsMap.values()) {
                    if (!subscriberGroup.isLocked()) {
                        PeekingIterator<MessageWithGroup> groupIterator = subscriberGroupIteratorMap.get(subscriberGroup);
                        if (groupIterator.hasNext()) {
                            currIterator = groupIterator;
                            refreshed = true;
                            break;
                        }
                    }
                }
                return refreshed;
            }

            @Override
            public MessageWithGroup peek() {
                return currIterator.peek();
            }

            @Override
            public MessageWithGroup next() {
                return currIterator.next();
            }

            @Override
            public void remove() {
                currIterator.remove();
            }

            @Override
            public boolean hasNext() {
                return refresh() && currIterator.hasNext();
            }
        };
    }
}
