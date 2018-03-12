package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.subscribers.GroupedPartSubscriber;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

@Slf4j
public class SubscriberIteratorTest {

    private SubscriberIterator subscriberIterator;
    private TopicPartDataManager topicPartDataManager;
    private PartSubscription partSubscription;
    private PartSubscriber partSubscriber;

    @BeforeMethod
    public void setUp() {
        topicPartDataManager = new InMemoryTopicPartDataManager();
        //partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.unGroupedSubscription, (short) 0);
        partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.groupedSubscription, (short) 0);
        SubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);

        //PartSubscriber partSubscriber = new UnGroupedPartSubscriber(subPartDataManager, partSubscription);
        partSubscriber = new GroupedPartSubscriber(topicPartDataManager, subPartDataManager, partSubscription);
        List<PartSubscriber> partSubscribers = Lists.newArrayList(partSubscriber);
        subscriberIterator = new SubscriberIterator(partSubscribers);
    }

    @Test
    public void shouldIterate_OverMessages() throws InterruptedException {
        int noOfMessages = 10;
        List<Message> messages = generateMessages(noOfMessages);
        List<com.flipkart.vbroker.client.MessageMetadata> messageMetadataList = addPartData(messages);
        assertEquals(messageMetadataList.size(), noOfMessages);

        int count = 0;
        MsgIterator<IterableMessage> iterator = subscriberIterator;
        while (iterator.hasNext()) {
            IterableMessage iterableMessage = iterator.peek();
            log.info("Peeking msg {}", iterableMessage.getMessage().messageId());
            iterator.next();
            count++;
        }

        CountDownLatch latch = new CountDownLatch(1);
        int moreNoOfMessages = 5;

        /*
         * now add more messages to topicPartData after creating the iterator
         * the iterator should then traverse these messages as well
         */
        new Thread(() -> {
            List<Message> moreMessages = generateMessages(moreNoOfMessages);
            List<com.flipkart.vbroker.client.MessageMetadata> moreMetadataList = addPartData(moreMessages);
            assertEquals(moreMetadataList.size(), moreMessages.size());

            latch.countDown();
        }).start();

        latch.await();
        partSubscriber.refreshSubscriberMetadata();

        while (iterator.hasNext()) {
            IterableMessage iterableMessage = iterator.peek();
            log.info("Peeking msg {}", iterableMessage.getMessage().messageId());
            iterator.next();
            count++;
        }

        assertEquals(count, noOfMessages + moreNoOfMessages);
    }

    private List<com.flipkart.vbroker.client.MessageMetadata> addPartData(List<Message> messages) {
        return messages.stream()
            .map(message -> topicPartDataManager.addMessage(partSubscription.getTopicPartition(), message)
                .toCompletableFuture().join())
            .collect(Collectors.toList());
    }

    private List<Message> generateMessages(int noOfMessages) {
        return IntStream.range(0, noOfMessages)
            .mapToObj(i -> MessageStore.getRandomMsg("group_" + i))
            .collect(Collectors.toList());
    }
}