package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@EqualsAndHashCode(exclude = {"subPartData"})
@ToString(exclude = {"subPartData"})
public class UnGroupedPartSubscriber implements IPartSubscriber {

    private final SubPartData subPartData;
    @Getter
    private final PartSubscription partSubscription;

    public UnGroupedPartSubscriber(SubPartData subPartData,
                                   PartSubscription partSubscription) {
        this.subPartData = subPartData;
        this.partSubscription = partSubscription;
        log.info("Creating UnGroupedPartSubscriber object for partSubscription {}", partSubscription);
    }

    @Override
    public void refreshSubscriberMetadata() {
        log.debug("Ignoring refresh of subscriber metadata for un-grouped part subscriber");
    }

    @Override
    public PeekingIterator<MessageWithMetadata> iterator() {
        log.info("Creating UnGroupedPartSubscriber iterator for partSub {}", partSubscription);
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<MessageWithMetadata>> nextIterator() {
                return subPartData.getIterator(QType.MAIN);
            }
        };
    }

    @Override
    public PeekingIterator<MessageWithMetadata> sidelineIterator() {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<MessageWithMetadata>> nextIterator() {
                return subPartData.getIterator(QType.SIDELINE);
            }
        };
    }

    @Override
    public PeekingIterator<MessageWithMetadata> retryIterator(int retryQNo) {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<MessageWithMetadata>> nextIterator() {
                QType qType = QType.retryQType(retryQNo);
                return subPartData.getIterator(qType);
            }
        };
    }
}
