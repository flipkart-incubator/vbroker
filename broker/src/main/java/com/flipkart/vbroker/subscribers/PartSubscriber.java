package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
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
public class PartSubscriber implements Iterable<IMessageWithGroup> {
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
    public void refreshSubscriberGroups() {
        log.debug("Refreshing SubscriberGroups for part-subscriber {} for topic-partition {}",
            partSubscription.getId(), partSubscription.getTopicPartition().getId());
        TopicPartition topicPartition = partSubscription.getTopicPartition();

        topicPartDataManager.getUniqueGroups(topicPartition).thenAccept(uniqueMsgGroups -> {
            Set<String> uniqueSubscriberGroups = subscriberGroupsMap.keySet();
            Sets.SetView<String> difference = Sets.difference(uniqueMsgGroups, uniqueSubscriberGroups);
            difference.iterator().forEachRemaining(group -> {
                MessageGroup messageGroup = new MessageGroup(group, topicPartition);
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
                subscriberGroupsMap.put(group, subscriberGroup);
                subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            });
        });
    }

    @Override
    public PeekingIterator<IMessageWithGroup> iterator() {
        return new PeekingIterator<IMessageWithGroup>() {
            PeekingIterator<IMessageWithGroup> currIterator;

            boolean refresh() {
                boolean refreshed = false;
                if (currIterator != null
                    && currIterator.hasNext()
                    && !currIterator.peek().isLocked()) {
                    log.trace("Group {} is not locked, hence returning true", currIterator.peek().getMessage().groupId());
                    return true;
                }

                if (log.isDebugEnabled()) {
                    List<String> groupIds = subscriberGroupsMap.values().stream()
                        .map(SubscriberGroup::getGroupId)
                        .collect(Collectors.toList());
                    log.debug("SubscriberGroupsMap values: {}", Collections.singletonList(groupIds));
                }

                for (SubscriberGroup subscriberGroup : subscriberGroupsMap.values()) {
                    log.trace("SubscriberGroup {} locked status: {}", subscriberGroup.getGroupId(), subscriberGroup.isLocked());
                    if (!subscriberGroup.isLocked()) {
                        PeekingIterator<IMessageWithGroup> groupIterator = subscriberGroupIteratorMap.get(subscriberGroup);
                        log.trace("Iterator {} for subscriberGroup {} hasNext: {}", groupIterator, subscriberGroup.getGroupId(), groupIterator.hasNext());
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
            public IMessageWithGroup peek() {
                return currIterator.peek();
            }

            @Override
            public IMessageWithGroup next() {
                return currIterator.next();
            }

            @Override
            public void remove() {
                currIterator.remove();
            }

            @Override
            public boolean hasNext() {
                try {
                    if (refresh()) {
                        return currIterator.hasNext();
                    }
                } catch (Exception e) {
                    log.error("Exception in refresh/hasNext", e);
                }
                return false;
            }
        };
    }
}
