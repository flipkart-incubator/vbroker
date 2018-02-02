package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@EqualsAndHashCode(exclude = {"subscriberGroupsMap"})
@ToString
public class PartSubscriber implements Iterable<SubscriberGroup> {

    @Getter
    private final PartSubscription partSubscription;
    @Getter
    private final ConcurrentMap<String, SubscriberGroup> subscriberGroupsMap = new ConcurrentHashMap<>();

    public PartSubscriber(PartSubscription partSubscription) {
        log.info("Creating new PartSubscriber for part-subscription {}", partSubscription);
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
    public void
    refreshSubscriberGroups() {
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
            }
        });
    }

    public List<Message> getMessages(int noOfMessagesToFetch) {
        List<Message> fetchedMessages = new LinkedList<>();
        PeekingIterator<SubscriberGroup> groupIterator = iterator();

        int m = 0;
        while (groupIterator.hasNext() && m < noOfMessagesToFetch) {
            SubscriberGroup group = groupIterator.peek();
            log.debug("Fetching messages of group {} for topic {}", group.getGroupId(), partSubscription.getTopicPartition().getId());

            PeekingIterator<Message> messageIterator = group.iterator();
            while (messageIterator.hasNext()) {
                Message msg = messageIterator.peek();
                log.debug("Peeking Message with msg_id: {} and group_id: {}", msg.messageId(), msg.groupId());
                fetchedMessages.add(msg);
                m++;
                messageIterator.next();
            }
            groupIterator.next();
        }
        return fetchedMessages;
    }
}
