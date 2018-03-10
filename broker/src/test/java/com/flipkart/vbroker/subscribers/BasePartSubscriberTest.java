package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.MsgIterator;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.services.InMemorySubscriptionService;
import com.flipkart.vbroker.services.InMemoryTopicService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

@Slf4j
@Setter
public class BasePartSubscriberTest {

    private PartSubscriber partSubscriber;
    private PartSubscription partSubscription;
    private SubPartDataManager subPartDataManager;
    private TopicPartDataManager topicPartDataManager;
//
//    @BeforeMethod
//    public abstract void setUp();

    @BeforeMethod
    public void setUp() {
        //PartSubscription partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.unGroupedSubscription, (short) 0);
        PartSubscription partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.groupedSubscription, (short) 0);
        InMemoryTopicPartDataManager topicPartDataManager = new InMemoryTopicPartDataManager();
        InMemorySubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);

        TopicService topicService = new InMemoryTopicService();
        SubscriptionService subscriptionService = new InMemorySubscriptionService(topicService, topicPartDataManager, subPartDataManager);
        partSubscriber = subscriptionService.getPartSubscriber(partSubscription).toCompletableFuture().join();

        setPartSubscription(partSubscription);
        setTopicPartDataManager(topicPartDataManager);
        setSubPartDataManager(subPartDataManager);
        //setPartSubscriber(new UnGroupedPartSubscriber(subPartDataManager, partSubscription));
        setPartSubscriber(new GroupedPartSubscriber(topicPartDataManager, subPartDataManager, partSubscription));
    }

    @Test
    public void shouldIterateOver_NewMessages_DiffGroups() throws InterruptedException {
        int noOfMessages = 20;
        int moreNoOfMessages = 5;
        int count = addAndConsumeMessages(noOfMessages, moreNoOfMessages, true);

        assertEquals(count, noOfMessages + moreNoOfMessages);
    }

    @Test
    public void shouldIterateOver_NewMessages_SameGroup() throws InterruptedException {
        int noOfMessages = 5;
        int moreNoOfMessages = 2;
        int count = addAndConsumeMessages(noOfMessages, moreNoOfMessages, false);

        assertEquals(count, noOfMessages + moreNoOfMessages);
    }

    //returns the count of consumed messages
    private int addAndConsumeMessages(int noOfMessages, int moreNoOfMessages, boolean diffGroups) throws InterruptedException {
        List<Message> messages = generateMessages(noOfMessages, diffGroups);
        List<com.flipkart.vbroker.client.MessageMetadata> messageMetadataList = addPartData(messages);
        assertEquals(messageMetadataList.size(), noOfMessages);

        int count = 0;
        PartSubscriberIterator iterator = partSubscriber.iterator(QType.MAIN);
        /*
         * now add more messages to topicPartData after creating the iterator
         * the iterator should then traverse these messages as well
         */
        addMessagesToPartData(moreNoOfMessages, diffGroups);
        partSubscriber.refreshSubscriberMetadata();

        while (iterator.hasNext()) {
            //IterableMessage iterableMessage = iterator.peek();
            //log.info("Peeking msg {} with group {}", iterableMessage.getMessage().messageId(), iterableMessage.getMessage().groupId());
            iterator.next();
            count++;
        }
        return count;
    }

    @Test
    public void shouldIterateOver_SidelinedMessages() throws InterruptedException {
        int count = 0;
        MsgIterator<IterableMessage> iterator = partSubscriber.iterator(QType.MAIN);
        MsgIterator<IterableMessage> sidelineIterator = partSubscriber.iterator(QType.SIDELINE);

        int moreNoOfMessages = 5;
        addMessagesToPartData(moreNoOfMessages, true);
        partSubscriber.refreshSubscriberMetadata();

        while (iterator.hasNext()) {
            IterableMessage iterableMessage = iterator.peek();
            subPartDataManager.sideline(partSubscription, iterableMessage).toCompletableFuture().join();
            iterator.next();
            count++;
        }
        assertEquals(count, moreNoOfMessages);
        log.info("Sidelined messages, now iterating over them");

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
    public void shouldIterateOver_RetryMessages_MainQToRQ1() throws InterruptedException {
        shouldRetryMessagesToCorrespondingQType(QType.RETRY_1, QType.MAIN);
    }

    @Test
    public void shouldIterateOver_RetryMessages_RQ1ToRQ2() throws InterruptedException {
        shouldRetryMessagesToCorrespondingQType(QType.RETRY_2, QType.RETRY_1);
    }

    @Test
    public void shouldIterateOver_RetryMessages_RQ2ToRQ3() throws InterruptedException {
        shouldRetryMessagesToCorrespondingQType(QType.RETRY_3, QType.RETRY_2);
    }

    @Test
    public void shouldIterateOver_MoveFromRQ3ToSQ() throws InterruptedException {
        MsgIterator<IterableMessage> iterator = partSubscriber.iterator(QType.MAIN);
        MsgIterator<IterableMessage> sidelineIterator = partSubscriber.iterator(QType.SIDELINE);

        shouldRetryMessages(QType.RETRY_3, iterator, sidelineIterator);
    }

    private void shouldRetryMessagesToCorrespondingQType(QType destQType, QType currQType) throws InterruptedException {
        MsgIterator<IterableMessage> iterator = partSubscriber.iterator(QType.MAIN);
        MsgIterator<IterableMessage> retryIterator = partSubscriber.iterator(destQType);

        shouldRetryMessages(currQType, iterator, retryIterator);
    }

    private void shouldRetryMessages(QType srcQType,
                                     MsgIterator<IterableMessage> srcQTypeIterator,
                                     MsgIterator<IterableMessage> destQTypeIterator) throws InterruptedException {
        int count = 0;
        int moreNoOfMessages = 5;
        addMessagesToPartData(moreNoOfMessages, true);
        partSubscriber.refreshSubscriberMetadata();

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
            log.info("Consuming QType {} message {}", currIterableMessage.getQType(), currIterableMessage.getMessage().messageId());
            count++;
            destQTypeIterator.next();
        }
        assertEquals(count, moreNoOfMessages);
    }

    private void addMessagesToPartData(int moreNoOfMessages, boolean diffGroups) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            List<Message> moreMessages = generateMessages(moreNoOfMessages, diffGroups);
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

    private List<Message> generateMessages(int noOfMessages, boolean diffGroups) {
        return IntStream.range(0, noOfMessages)
            .mapToObj(i -> {
                String groupId = "group_0";
                if (diffGroups) {
                    groupId = "group_" + i;
                }
                return MessageStore.getRandomMsg(groupId);
            })
            .collect(Collectors.toList());
    }
}
