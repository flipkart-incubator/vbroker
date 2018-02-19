package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.google.common.collect.PeekingIterator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class UnGroupedPartSubscriber implements IPartSubscriber {

    @Getter
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    private final AtomicInteger seqNo = new AtomicInteger();

    public UnGroupedPartSubscriber(TopicPartDataManager topicPartDataManager,
                                   PartSubscription partSubscription) {
        this.topicPartDataManager = topicPartDataManager;
        this.partSubscription = partSubscription;
    }

    @Override
    public void refreshSubscriberMetadata() {
        log.debug("Ignoring refresh of subscriber metadata for un-grouped part subscriber");
    }

    @Override
    public PeekingIterator<IMessageWithGroup> iterator() {
        return new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IMessageWithGroup>> nextIterator() {
                PeekingIterator<Message> iterator = topicPartDataManager
                    .getIterator(partSubscription.getTopicPartition(), seqNo.get());

                PeekingIterator<IMessageWithGroup> peekingIterator = new PeekingIterator<IMessageWithGroup>() {
                    @Override
                    public IMessageWithGroup peek() {
                        return new UngroupedMessageWithGroup(iterator.peek());
                    }

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public IMessageWithGroup next() {
                        return new UngroupedMessageWithGroup(iterator.next());
                    }

                    @Override
                    public void remove() {
                        throw new NotImplementedException();
                    }
                };

                return Optional.of(peekingIterator);
            }
        };
    }
}
