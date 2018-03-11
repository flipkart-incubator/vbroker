package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
        return CompletableFuture.supplyAsync(() -> {
            return failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            //return failedGroups.get(qType);
        });
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
        return new DataIteratorImpl(qType);
    }

    private Optional<SubscriberGroupIterator<IterableMessage>> fetchIterator(QType qType) {
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

        List<String> valuesComputed = values.toCompletableFuture().join();  //FIX this!
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
            .filter(group -> !group.isLocked())
            .filter(group -> qType.equals(group.getQType()))
            .filter(group -> groupQTypeIteratorTable.contains(group, qType))
            .map(subscriberGroup -> groupQTypeIteratorTable.get(subscriberGroup, qType))
            .filter(Iterator::hasNext)
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
    private class DataIteratorImpl implements DataIterator<IterableMessage> {
        private final QType qType;
        private Optional<SubscriberGroupIterator<IterableMessage>> iteratorOpt;

        DataIteratorImpl(QType qType) {
            this.qType = qType;
            iteratorOpt = fetchIterator(qType);
        }

        @Override
        public String name() {
            return iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::name)
                .orElse("data_iterator_impl_qType_" + qType);
        }

        @Override
        public IterableMessage peek() {
            return iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::peek)
                .orElse(null);
        }

        @Override
        public boolean hasNext() {
            if (!hasNext2()) {
                iteratorOpt = fetchIterator(qType);
            }
            return hasNext2();
        }

        private boolean hasNext2() {
            return iteratorOpt
                .map(SubscriberGroupIterator<IterableMessage>::hasNext)
                .orElse(false);
        }

        @Override
        public IterableMessage next() {
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
