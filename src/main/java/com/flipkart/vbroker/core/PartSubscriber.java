package com.flipkart.vbroker.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class PartSubscriber implements Iterable<SubscriberGroup> {

    @Getter
    private final PartSubscription partSubscription;
    private final ConcurrentMap<String, SubscriberGroup> subscriberGroupsMap = new ConcurrentHashMap<>();

    public PartSubscriber(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
        refreshSubscriberGroups();
    }

    @Override
    public PeekingIterator<SubscriberGroup> iterator() {
        return Iterators.peekingIterator(subscriberGroupsMap.values().iterator());
    }

    /**
     * Call this method to keep subscriberGroups in sync with messageGroups at any point
     */
    public void refreshSubscriberGroups() {
        log.info("Refreshing SubscriberGroups");
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        Set<String> uniqueMsgGroups = topicPartition.getUniqueGroups();
        Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();
        uniqueMsgGroups.removeAll(uniqueSubscriberGroups);
        uniqueMsgGroups.forEach(group -> {
            Optional<MessageGroup> messageGroup = topicPartition.getMessageGroup(group);
            if (messageGroup.isPresent()) {
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup.get(), topicPartition);
                subscriberGroupsMap.put(group, subscriberGroup);
            }
        });
    }
}
