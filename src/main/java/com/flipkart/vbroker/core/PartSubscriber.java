package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
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
@EqualsAndHashCode(exclude = {"subscriberGroupsMap", "parallelism"})
@ToString
public class PartSubscriber {
    public static final Integer DEFAULT_PARALLELISM = 5;
    public static final Integer MAX_GROUPS_IN_ITERATOR = 100;

    @Getter
    private final PartSubscription partSubscription;
    private final ConcurrentMap<String, SubscriberGroup> subscriberGroupsMap = new ConcurrentHashMap<>();
    @Getter
    private final int parallelism;
    private final Queue<PeekingIterator<Message>>[] messageQueues;

    public PartSubscriber(PartSubscription partSubscription, int parallelism) {
        log.info("Creating new PartSubscriber for part-subscription {}", partSubscription);
        this.partSubscription = partSubscription;
        this.parallelism = parallelism;
        this.messageQueues = (Queue<PeekingIterator<Message>>[]) new Queue[parallelism];
        for (int i = 0; i < parallelism; i++) {
            messageQueues[i] = new ArrayBlockingQueue<>(MAX_GROUPS_IN_ITERATOR);
        }

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

    public class MuxMessageIterator<Message> implements PeekingIterator<Message> {

        private final Queue<PeekingIterator<Message>> iteratorQueue;
        private PeekingIterator<Message> currIterator;

        public MuxMessageIterator(Queue<PeekingIterator<Message>> iteratorQueue) {
            this.iteratorQueue = iteratorQueue;
            currIterator = iteratorQueue.poll();
        }

        @Override
        public Message peek() {
            return currIterator.peek();
        }

        @Override
        public boolean hasNext() {
            updateIterator();
            return currIterator.hasNext();
        }

        private void updateIterator() {
            if (!currIterator.hasNext()) {
                iteratorQueue.add(currIterator);
                iteratorQueue.remove();
                while (!iteratorQueue.isEmpty()) {
                    PeekingIterator<Message> probableIterator = iteratorQueue.poll();
                    if (probableIterator.hasNext()) {
                        currIterator = probableIterator;
                    } else {
                        iteratorQueue.add(probableIterator);
                    }
                }
            }
        }

        @Override
        public Message next() {
            return currIterator.next();
        }

        @Override
        public void remove() {
            currIterator.remove();
        }
    }

    public List<PeekingIterator<Message>> iterators() {
        List<PeekingIterator<Message>> reqIterators = new ArrayList<>();
        int k = 0;
        for (Map.Entry<String, SubscriberGroup> entry : subscriberGroupsMap.entrySet()) {
            SubscriberGroup subscriberGroup = entry.getValue();
            PeekingIterator<Message> groupMsgIterator = subscriberGroup.iterator();
            messageQueues[k].add(groupMsgIterator);
            k++;
        }

        for (Queue<PeekingIterator<Message>> messageQueue : messageQueues) {
            MuxMessageIterator<Message> muxIterator = new MuxMessageIterator<>(messageQueue);
            reqIterators.add(muxIterator);
        }

        return reqIterators;
    }
}
