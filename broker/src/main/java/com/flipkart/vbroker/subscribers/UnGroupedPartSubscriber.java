package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.iterators.VIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@EqualsAndHashCode(exclude = {"subPartDataManager"})
@ToString(exclude = {"subPartDataManager"})
public class UnGroupedPartSubscriber implements PartSubscriber {

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
    public VIterator<IterableMessage> iterator() {
        log.info("Creating UnGroupedPartSubscriber iterator for partSub {}", partSubscription);
        return subPartDataManager.getIterator(partSubscription, QType.MAIN);
    }

    @Override
    public VIterator<IterableMessage> sidelineIterator() {
        VIterator<IterableMessage> iterator =
            subPartDataManager.getIterator(partSubscription, QType.SIDELINE);

        return new PartSubscriberIterator() {
            @Override
            protected Optional<VIterator<IterableMessage>> nextIterator() {
                return Optional.of(iterator);
            }
        };
    }

    @Override
    public VIterator<IterableMessage> retryIterator(int retryQNo) {
        QType qType = QType.retryQType(retryQNo);
        VIterator<IterableMessage> iterator =
            subPartDataManager.getIterator(partSubscription, qType);
        return new PartSubscriberIterator() {
            @Override
            protected Optional<VIterator<IterableMessage>> nextIterator() {
                return Optional.of(iterator);
            }
        };
    }
}
