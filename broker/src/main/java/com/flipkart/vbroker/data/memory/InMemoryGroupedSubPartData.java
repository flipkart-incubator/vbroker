package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.iterators.MsgIterators;
import com.flipkart.vbroker.iterators.SubscriberGroupIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class InMemoryGroupedSubPartData implements SubPartData {

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<QType, List<String>> failedGroups = new LinkedHashMap<>();
    private final Table<SubscriberGroup, QType, SubscriberGroupIterator<IterableMessage>> groupQTypeIteratorTable
        = HashBasedTable.create();

    public InMemoryGroupedSubPartData(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        return CompletableFuture.supplyAsync(() -> {
            subscriberGroupsMap.put(subscriberGroup.getGroupId(), subscriberGroup);
            //subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            groupQTypeIteratorTable.put(subscriberGroup, QType.MAIN, subscriberGroup.iterator(QType.MAIN));

            //TODO: we should ideally set msg_id
            return new MessageMetadata(subscriberGroup.getGroupId(), subscriberGroup.getTopicPartition().getTopicId(),
                subscriberGroup.getTopicPartition().getId(), new Random().nextInt());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(subscriberGroupsMap::keySet);
    }

    private CompletionStage<List<String>> getFailedGroups(QType qType) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        List<String> currFailedGroups = failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>());
        future.complete(currFailedGroups);
        return future;
    }

    @Override
    public CompletionStage<Void> sideline(IterableMessage iterableMessage) {
        return appendFailedGroup(iterableMessage);
    }

    @Override
    public CompletionStage<Void> retry(IterableMessage iterableMessage) {
        return appendFailedGroup(iterableMessage);
    }

    private synchronized CompletionStage<Void> appendFailedGroup(IterableMessage iterableMessage) {
        QType qType = iterableMessage.getQType();
        log.debug("Appending failed group {} to QType {}", iterableMessage.getGroupId(), iterableMessage.getQType());

        return getFailedGroups(qType).thenAccept(groups -> {
            List<String> fGroups = failedGroups.get(qType);
            fGroups.add(iterableMessage.getGroupId());
            failedGroups.put(qType, fGroups);

            SubscriberGroup subscriberGroup = subscriberGroupsMap.get(iterableMessage.getGroupId());
            if (!groupQTypeIteratorTable.contains(subscriberGroup, qType)) {
                //don't create a new iterator if already failed iterator exists
                groupQTypeIteratorTable.put(subscriberGroup, qType, subscriberGroup.iterator(qType));
            }
        });
    }

    @Override
    public DataIterator<IterableMessage> getIterator(String groupId) {
        SubscriberGroup subscriberGroup = subscriberGroupsMap.get(groupId);
        SubscriberGroupIterator<IterableMessage> subscriberGroupIterator =
            groupQTypeIteratorTable.get(subscriberGroup, QType.MAIN);
        return MsgIterators.dataIterator(subscriberGroupIterator);
    }

    @Override
    public DataIterator<IterableMessage> getIterator(QType qType) {
        //Optional<SubscriberGroup.SubscriberGroupIteratorImpl> iteratorOpt = fetchIterators(qType);
        return new DataIteratorImpl(qType, false);
    }

    @Override
    public List<IterableMessage> poll(QType qType, int maxRecords, long pollTimeMs) {
        DataIterator<IterableMessage> iterator = new DataIteratorImpl(qType, true);
        List<IterableMessage> iterableMessages = new ArrayList<>();
        int noOfRecords = 0;
        long startTimeMs = System.currentTimeMillis();

        while (true) {
            long elapsedTimeMs = System.currentTimeMillis() - startTimeMs;
            if ((noOfRecords < maxRecords) && (elapsedTimeMs <= pollTimeMs) && iterator.hasNext()) {
                //success
                iterableMessages.add(iterator.next());
                noOfRecords++;
            } else {
                log.info("ElapsedTimeMs: {}; pollTimeMs: {}; noOfRecords: {}; maxRecords: {}; iteratorHasNext: {}",
                    elapsedTimeMs, pollTimeMs, noOfRecords, maxRecords, iterator.hasNext());
                break;
            }
        }

        return iterableMessages;
    }

    @Override
    public CompletionStage<Void> commitOffset(String group, int offset) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SubscriberGroup subscriberGroup = subscriberGroupsMap.get(group);
        if (isNull(subscriberGroup)) {
            future.completeExceptionally(new VBrokerException("SubscriberGroup not found for group: " + group));
        } else {
            subscriberGroup.setOffset(offset);
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletionStage<Integer> getOffset(String group) {
        //TODO: fix the case where upon sidelining, the group message that had 4xx should be not moved by next() in the iterator so that it's not lost
        return CompletableFuture.supplyAsync(() -> {
            SubscriberGroup subscriberGroup = subscriberGroupsMap.get(group);
            if (isNull(subscriberGroup)) {
                return -1;
            }
            return subscriberGroup.getOffset();
        });
    }

    private List<SubscriberGroupIterator<IterableMessage>> fetchIterators(QType qType,
                                                                          boolean manualSeqNoManagement) {
        log.debug("Re-fetching iterator for qType {}", qType);
        Stream<String> values;
        switch (qType) {
            case MAIN:
                values = subscriberGroupsMap.values()
                    .stream()
                    .map(SubscriberGroup::getGroupId);
                break;
            default:
                //reviewed - this is a blocking call but mostly safe
                values = getFailedGroups(qType).toCompletableFuture().join().stream();
                break;
        }

        if (log.isDebugEnabled()) {
            List<String> groupIds = values
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getGroupId)
                .collect(Collectors.toList());
            log.debug("SubscriberGroupsMap values for qType {} are: {}", qType, Collections.singletonList(groupIds));
        }

        return values.map(subscriberGroupsMap::get)
            .filter(SubscriberGroup::isUnlocked)
            .filter(group -> qType.equals(group.getQType()))
            .filter(group -> groupQTypeIteratorTable.contains(group, qType))
            .map(subscriberGroup -> {
                if (manualSeqNoManagement) {
                    return subscriberGroup.newIterator(qType, subscriberGroup.getOffset());
                }
                return groupQTypeIteratorTable.get(subscriberGroup, qType);
            })
            .filter(iterator -> nonNull(iterator) && iterator.hasNext() && iterator.isUnlocked())
            .collect(Collectors.toList());
    }

    @Override
    public CompletionStage<Integer> getLag() {
        return getUniqueGroups().thenCompose(groups -> {
            List<CompletionStage<Integer>> lagStages = groups.stream()
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getLag)
                .collect(Collectors.toList());

            return CompletionStageUtils.listOfStagesToStageOfList(lagStages).thenApply(lags ->
                lags.stream()
                    .reduce(0, (lag1, lag2) -> lag1 + lag2));
        });
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @AllArgsConstructor
    @NotThreadSafe
    private class DataIteratorImpl implements DataIterator<IterableMessage> {
        private final QType qType;
        private final boolean manualSeqNoManagement;
        private Optional<SubscriberGroupIterator<IterableMessage>> iteratorOpt = Optional.empty();
        private IterableMessage lastPeekedMsg;
        private final Queue<SubscriberGroupIterator<IterableMessage>> iteratorQueue = new ArrayDeque<>();

        DataIteratorImpl(QType qType, boolean manualSeqNoManagement) {
            this.qType = qType;
            //fetchIterators(qType, manualSeqNoManagement).forEach(iteratorQueue::offer);
            this.manualSeqNoManagement = manualSeqNoManagement;
        }

        @Override
        public boolean isUnlocked() {
            //return !nonNull(lastPeekedMsg) || lastPeekedMsg.isUnlocked();
            return true;
        }

        @Override
        public String name() {
            return iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::name)
                .orElse("data_iterator_impl_qType_" + qType);
        }

        @Override
        public IterableMessage peek() {
            lastPeekedMsg = iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::peek)
                .orElse(null);
            return lastPeekedMsg;
        }

        @Override
        public boolean hasNext() {
            if (!hasNext2()) {
                if (iteratorQueue.isEmpty()) {
                    fetchIterators(qType, manualSeqNoManagement).forEach(iteratorQueue::offer);
                }

                while (!iteratorQueue.isEmpty()) {
                    SubscriberGroupIterator<IterableMessage> headIterator = iteratorQueue.peek();
                    if (headIterator.isUnlocked() && headIterator.hasNext()) {
                        iteratorOpt = Optional.of(headIterator);
                        iteratorQueue.poll();
                        break;
                    } else {
                        iteratorQueue.offer(iteratorQueue.poll());
                    }
                }
                //iteratorOpt = fetchIterators(qType, manualSeqNoManagement);
                iteratorOpt.ifPresent(it -> log.debug("Changed iterator to {}", it.name()));
            }
            //recompute again
            return hasNext2();
        }

        private boolean hasNext2() {
            return iteratorOpt
                .map(iterator -> iterator.isUnlocked() && iterator.hasNext())
                //.map(SubscriberGroupIterator<IterableMessage>::hasNext)
                .orElse(false);
        }

        @Override
        public IterableMessage next() {
            log.info("Moving to next message for group {}", peek().getGroupId());
            return iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::next)
                .orElse(null);
        }

        @Override
        public void remove() {
            iteratorOpt.ifPresent(SubscriberGroupIterator<IterableMessage>::remove);
        }
    }
}
