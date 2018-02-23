package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

@Slf4j
public class UnGroupedGroupedPartSubscriberTest {

    private UnGroupedPartSubscriber partSubscriber;

    private PartSubscription partSubscription;
    private SubPartDataManager subPartDataManager;
    private TopicPartDataManager topicPartDataManager;

    @BeforeMethod
    public void setUp() {
        partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.unGroupedSubscription, (short) 0);
        topicPartDataManager = new InMemoryTopicPartDataManager();
        subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);

        partSubscriber = new UnGroupedPartSubscriber(subPartDataManager, partSubscription);
    }

    @Test
    public void shouldIterateOver_NewMessages() throws InterruptedException {
        int noOfMessages = 10;
        List<Message> messages = generateMessages(noOfMessages);
        List<com.flipkart.vbroker.client.MessageMetadata> messageMetadataList = addPartData(messages);
        assertEquals(messageMetadataList.size(), noOfMessages);

        int count = 0;
        PeekingIterator<IterableMessage> iterator = partSubscriber.iterator();
        /*
         * now add more messages to topicPartData after creating the iterator
         * the iterator should then traverse these messages as well
         */
        int moreNoOfMessages = 5;
        addMessagesToPartData(moreNoOfMessages);

        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        assertEquals(count, noOfMessages + moreNoOfMessages);
    }

    @Test
    public void shouldIterateOver_SidelinedMessages() throws InterruptedException {
        int count = 0;
        PeekingIterator<IterableMessage> iterator = partSubscriber.iterator();
        PeekingIterator<IterableMessage> sidelineIterator = partSubscriber.sidelineIterator();

        int moreNoOfMessages = 5;
        addMessagesToPartData(moreNoOfMessages);

        while (iterator.hasNext()) {
            IterableMessage iterableMessage = iterator.peek();
            subPartDataManager.sideline(partSubscription, iterableMessage).toCompletableFuture().join();
            iterator.next();
            count++;
        }
        assertEquals(count, moreNoOfMessages);

        count = 0;
        while (sidelineIterator.hasNext()) {
            IterableMessage currIterableMessage = sidelineIterator.peek();
            log.info("Consuming sidelined message {}", currIterableMessage.getMessage().messageId());
            count++;
            sidelineIterator.next();
        }
        assertEquals(count, moreNoOfMessages);
    }

    @Test
    public void shouldIterateOver_RetryMessages() throws InterruptedException {
        shouldRetryMessagesToCorrespondingQType(1, QType.MAIN);
        shouldRetryMessagesToCorrespondingQType(2, QType.RETRY_1);
        shouldRetryMessagesToCorrespondingQType(3, QType.RETRY_2);
    }

    @Test
    public void shouldIterateOver_MoveFromRQ3ToSQ() throws InterruptedException {
        PeekingIterator<IterableMessage> iterator = partSubscriber.iterator();
        PeekingIterator<IterableMessage> sidelineIterator = partSubscriber.sidelineIterator();

        shouldRetryMessages(QType.RETRY_3, iterator, sidelineIterator);
    }

    private void shouldRetryMessagesToCorrespondingQType(int destRetryNo, QType currQType) throws InterruptedException {
        PeekingIterator<IterableMessage> iterator = partSubscriber.iterator();
        PeekingIterator<IterableMessage> retryIterator = partSubscriber.retryIterator(destRetryNo);

        shouldRetryMessages(currQType, iterator, retryIterator);
    }

    private void shouldRetryMessages(QType srcQType,
                                     PeekingIterator<IterableMessage> srcQTypeIterator,
                                     PeekingIterator<IterableMessage> destQTypeIterator) throws InterruptedException {
        int count = 0;
        int moreNoOfMessages = 5;
        addMessagesToPartData(moreNoOfMessages);

        while (srcQTypeIterator.hasNext()) {
            IterableMessage iterableMessage = srcQTypeIterator.peek();
            iterableMessage.setQType(srcQType);

            subPartDataManager.retry(partSubscription, iterableMessage).toCompletableFuture().join();
            srcQTypeIterator.next();
            count++;
        }
        assertEquals(count, moreNoOfMessages);

        count = 0;
        while (destQTypeIterator.hasNext()) {
            IterableMessage currIterableMessage = destQTypeIterator.peek();
            log.info("Consuming sidelined message {}", currIterableMessage.getMessage().messageId());
            count++;
            destQTypeIterator.next();
        }
        assertEquals(count, moreNoOfMessages);
    }

    private void addMessagesToPartData(int moreNoOfMessages) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            List<Message> moreMessages = generateMessages(moreNoOfMessages);
            List<com.flipkart.vbroker.client.MessageMetadata> moreMetadataList = addPartData(moreMessages);
            assertEquals(moreMetadataList.size(), moreMessages.size());

            latch.countDown();
        }).start();
        latch.await();
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