package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@EqualsAndHashCode(exclude = {"subPartDataManager"})
@ToString(exclude = {"subPartDataManager"})
public class UnGroupedPartSubscriber implements IPartSubscriber {

    private final SubPartDataManager subPartDataManager;
    @Getter
    private final PartSubscription partSubscription;

    public UnGroupedPartSubscriber(SubPartDataManager subPartDataManager,
                                   PartSubscription partSubscription) {
        this.subPartDataManager = subPartDataManager;
        this.partSubscription = partSubscription;
        log.info("Creating UnGroupedPartSubscriber object for partSubscription {}", partSubscription);
    }

    @Override
    public void refreshSubscriberMetadata() {
        log.debug("Ignoring refresh of subscriber metadata for un-grouped part subscriber");
    }

    @Override
    public PeekingIterator<IterableMessage> iterator() {
        log.info("Creating UnGroupedPartSubscriber iterator for partSub {}", partSubscription);
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                return subPartDataManager.getIterator(partSubscription, QType.MAIN);
            }
        };
    }

    @Override
    public PeekingIterator<IterableMessage> sidelineIterator() {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                return subPartDataManager.getIterator(partSubscription, QType.SIDELINE);
            }
        };
    }

    @Override
    public PeekingIterator<IterableMessage> retryIterator(int retryQNo) {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                QType qType = QType.retryQType(retryQNo);
                return subPartDataManager.getIterator(partSubscription, qType);
            }
        };
    }
}
