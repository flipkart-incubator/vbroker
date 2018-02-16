package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

@Slf4j
public class InMemoryTopicPartDataTest {

    private InMemoryTopicPartData topicPartData;

    @BeforeMethod
    public void setUp() {
        topicPartData = new InMemoryTopicPartData();
    }

    @Test
    public void shouldAddMessagesForSameGroup_AndIterate() {
        String groupId = "group-123";
        Message message_1 = MessageStore.getRandomMsg(groupId);
        topicPartData.addMessage(message_1).toCompletableFuture().join();

        PeekingIterator<Message> iterator = topicPartData.iteratorFrom(groupId, 0);

        new Thread(() -> {
            Message message_2 = MessageStore.getRandomMsg(groupId);
            Message message_3 = MessageStore.getRandomMsg(groupId);

            topicPartData.addMessage(message_2).toCompletableFuture().join();
            topicPartData.addMessage(message_3).toCompletableFuture().join();
        }).start();

        AtomicInteger count = new AtomicInteger();

        int i = 0;
        int noOfMessages = 3;
        while (i < 5 && count.get() < noOfMessages) {
            while (iterator.hasNext()) {
                iterator.next();
                count.getAndIncrement();
            }
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        assertEquals(count.get(), noOfMessages);
    }
}