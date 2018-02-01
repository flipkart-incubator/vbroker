package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.iterators.MuxMessageIterator;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@EqualsAndHashCode(exclude = {"subscriberGroupsMap"})
@ToString
public class PartSubscriber implements Iterable<Message> {
    public static final Integer DEFAULT_PARALLELISM = 5;
    public static final Integer MAX_GROUPS_IN_ITERATOR = 100;

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();

    public PartSubscriber(PartSubscription partSubscription) {
        log.info("Creating new PartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
        refreshSubscriberGroups();
    }

    public Set<SubscriberGroup> getSubscriberGroups() {
        return new HashSet<>(subscriberGroupsMap.values());
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
            }
        });
    }

    public List<Message> getMessages(int noOfMessagesToFetch) {
        throw new VBrokerException("Unimplemented");
//        List<Message> fetchedMessages = new LinkedList<>();
//        PeekingIterator<SubscriberGroup> groupIterator = Iterators.peekingIterator(getSubscriberGroups().iterator());
//
//        int m = 0;
//        while (groupIterator.hasNext() && m < noOfMessagesToFetch) {
//            SubscriberGroup group = groupIterator.peek();
//            log.debug("Fetching messageQueues of group {} for topic {}", group.getGroupId(), partSubscription.getTopicPartition().getId());
//
//            PeekingIterator<Message> messageIterator = group.iterator();
//            while (messageIterator.hasNext()) {
//                Message msg = messageIterator.peek();
//                log.debug("Peeking Message with msg_id: {} and group_id: {}", msg.messageId(), msg.groupId());
//                fetchedMessages.add(msg);
//                m++;
//                messageIterator.next();
//            }
//            groupIterator.next();
//        }
//        return fetchedMessages;
    }

    public void refreshParallelQueues() {
    }

    @Override
    public PeekingIterator<Message> iterator() {
        Queue<PeekingIterator<Message>> iteratorQueue = new ArrayDeque<>();
        for (SubscriberGroup subscriberGroup : subscriberGroupsMap.values()) {
            PeekingIterator<Message> groupMsgIterator = subscriberGroup.iterator();
            iteratorQueue.add(groupMsgIterator);
        }

        return new MuxMessageIterator<>(iteratorQueue);
    }
}
