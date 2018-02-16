package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class InMemoryTopicPartDataTest {

    private InMemoryTopicPartData topicPartData;

    @BeforeMethod
    public void setUp() {
        topicPartData = new InMemoryTopicPartData();
    }

    @Test
    public void shouldAddMessagesForSameGroup_AndIterate() {
        String groupId = "group-123";
        topicPartData.addMessage(MessageStore.getRandomMsg(groupId)).toCompletableFuture().join();
        topicPartData.addMessage(MessageStore.getRandomMsg(groupId)).toCompletableFuture().join();

        int count = 0;
        PeekingIterator<Message> iterator = topicPartData.iteratorFrom(groupId, 0);
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(count, 2);
    }
}