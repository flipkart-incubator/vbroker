package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@EqualsAndHashCode(exclude = {"subscriberGroupsMap"})
@ToString
public class PartSubscriber implements Iterable<Message> {
    public static final Integer DEFAULT_PARALLELISM = 5;
    public static final Integer MAX_GROUPS_IN_ITERATOR = 100;

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<SubscriberGroup, PeekingIterator<Message>> subscriberGroupIteratorMap = new LinkedHashMap<>();

    public PartSubscriber(PartSubscription partSubscription) {
        log.info("Creating new PartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
        refreshSubscriberGroups();
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
        log.info("Refreshing SubscriberGroups for part-subscriber {} for topic-partition {}",
                partSubscription.getId(), partSubscription.getTopicPartition().getId());
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        Set<String> uniqueMsgGroups = topicPartition.getUniqueGroups();
        Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();
        uniqueMsgGroups.removeAll(uniqueSubscriberGroups);
        uniqueMsgGroups.forEach(group -> {
            Optional<MessageGroup> messageGroup = topicPartition.getMessageGroup(group);
            if (messageGroup.isPresent()) {
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup.get(), topicPartition);
                subscriberGroupsMap.put(group, subscriberGroup);
                subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            }
        });
    }

    @Override
    public PeekingIterator<Message> iterator() {
        return new PeekingIterator<Message>() {
            PeekingIterator<Message> currIterator;

            boolean refresh() {
                boolean refreshed = false;
                if (currIterator != null && currIterator.hasNext()) {
                    return true;
                }

                for (SubscriberGroup subscriberGroup : subscriberGroupsMap.values()) {
                    if (!subscriberGroup.isLocked()) {
                        PeekingIterator<Message> groupIterator = subscriberGroupIteratorMap.get(subscriberGroup);
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
            public Message peek() {
                return currIterator.peek();
            }

            @Override
            public Message next() {
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
