package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;

public class InMemoryUnGroupedTopicPartDataTest {

    private InMemoryUnGroupedTopicPartData topicPartData;

    @BeforeMethod
    public void setUp() {
        topicPartData = new InMemoryUnGroupedTopicPartData();
    }

    @Test
    public void shouldAddMessages_AndIterateFromPos() throws InterruptedException {
        int noOfMessages = 5;
        List<Message> messages = generateMessages(noOfMessages);
        List<MessageMetadata> messageMetadataList = addPartData(messages);
        assertEquals(messageMetadataList.size(), noOfMessages);

        int iteratorFromPos = 2;
        int count = 0;
        PeekingIterator<Message> iterator = topicPartData.iteratorFrom(iteratorFromPos);

        CountDownLatch latch = new CountDownLatch(1);
        int moreNoOfMessages = 4;

        /*
         * now add more messages to topicPartData after creating the iterator
         * the iterator should then traverse these messages as well
         */
        new Thread(() -> {
            List<Message> moreMessages = generateMessages(moreNoOfMessages);
            List<MessageMetadata> moreMetadataList = addPartData(moreMessages);
            assertEquals(moreMetadataList.size(), moreMessages.size());

            latch.countDown();
        }).start();

        latch.await();

        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        assertEquals(count, noOfMessages + moreNoOfMessages - iteratorFromPos);
    }

    private List<MessageMetadata> addPartData(List<Message> messages) {
        return messages.stream()
            .map(message -> topicPartData.addMessage(message).toCompletableFuture().join())
            .collect(Collectors.toList());
    }

    private List<Message> generateMessages(int noOfMessages) {
        return IntStream.range(0, noOfMessages)
            .mapToObj(i -> MessageStore.getRandomMsg("group_" + i))
            .collect(Collectors.toList());
    }
}