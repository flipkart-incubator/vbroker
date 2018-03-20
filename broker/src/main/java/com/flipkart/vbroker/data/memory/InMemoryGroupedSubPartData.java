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

import static java.util.Objects.isNull;

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
        //Optional<SubscriberGroup.SubscriberGroupIteratorImpl> iteratorOpt = fetchIterator(qType);
        return new DataIteratorImpl(qType, false);
    }

    @Override
    public List<IterableMessage> poll(QType qType, int maxRecords, long pollTimeMs) {
        DataIterator<IterableMessage> iterator = new DataIteratorImpl(qType, true);
        List<IterableMessage> iterableMessages = new ArrayList<>();

        int noOfRecords = 0;
        long startTimeMs = System.currentTimeMillis();
        while ((noOfRecords < maxRecords) &&
            ((System.currentTimeMillis() - startTimeMs) >= pollTimeMs) &&
            iterator.hasNext()) {
            //success
            iterableMessages.add(iterator.next());
            noOfRecords++;
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

    private Optional<SubscriberGroupIterator<IterableMessage>> fetchIterator(QType qType,
                                                                             boolean manualSeqNoManagement) {
        log.debug("Re-fetching iterator for qType {}", qType);
        CompletionStage<List<String>> values;
        switch (qType) {
            case MAIN:
                values = CompletableFuture.supplyAsync(() -> subscriberGroupsMap.values()
                    .stream()
                    .map(SubscriberGroup::getGroupId)
                    .collect(Collectors.toList()));
                break;
            default:
                values = getFailedGroups(qType);
                break;
        }

        //reviewed - this is a blocking call but mostly safe
        List<String> valuesComputed = values.toCompletableFuture().join();
        if (log.isDebugEnabled()) {
            List<String> groupIds = valuesComputed //FIX this!!
                .stream()
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getGroupId)
                .collect(Collectors.toList());
            log.debug("SubscriberGroupsMap values for qType {} are: {}", qType, Collections.singletonList(groupIds));
        }

        return valuesComputed
            .stream()
            .map(subscriberGroupsMap::get)
            .filter(SubscriberGroup::isUnlocked)
            .filter(group -> qType.equals(group.getQType()))
            .filter(group -> groupQTypeIteratorTable.contains(group, qType))
            .map(subscriberGroup -> {
                if (manualSeqNoManagement) {
                    return subscriberGroup.newIterator(qType, subscriberGroup.getOffset());
                }
                return groupQTypeIteratorTable.get(subscriberGroup, qType);
            })
            .filter(iterator -> iterator.hasNext() && iterator.isUnlocked())
            .findFirst();
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
        private Optional<SubscriberGroupIterator<IterableMessage>> iteratorOpt;
        private IterableMessage lastPeekedMsg;

        DataIteratorImpl(QType qType, boolean manualSeqNoManagement) {
            this.qType = qType;
            iteratorOpt = fetchIterator(qType, manualSeqNoManagement);
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
                iteratorOpt = fetchIterator(qType, manualSeqNoManagement);
                iteratorOpt.ifPresent(it -> log.info("Changed iterator to {}", it.name()));
            }
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
