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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

public class UnGroupedPartSubscriberTest {

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
    public void shouldIterateOverNewMessages() throws InterruptedException {
        int noOfMessages = 10;
        List<Message> messages = generateMessages(noOfMessages);
        List<com.flipkart.vbroker.client.MessageMetadata> messageMetadataList = addPartData(messages);
        assertEquals(messageMetadataList.size(), noOfMessages);

        int count = 0;
        PeekingIterator<MessageWithMetadata> iterator = partSubscriber.iterator();

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

        while (iterator.hasNext()) {
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