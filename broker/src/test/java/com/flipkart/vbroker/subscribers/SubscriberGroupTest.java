package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Slf4j
public class SubscriberGroupTest {

    SubscriberGroup subscriberGroup;

    String group = "group-123";
    TopicPartition topicPartition = new TopicPartition((short) 0, (short) 101);
    MessageGroup messageGroup = new MessageGroup(group, topicPartition);
    PartSubscription partSubscription = new PartSubscription(topicPartition.getId(), topicPartition, DummyEntities.subscription1.id());

    @BeforeMethod
    public void setUp() {
        TopicPartDataManager topicPartDataManager = new InMemoryTopicPartDataManager();
        Message message_1 = MessageStore.getRandomMsg(group);
        Message message_2 = MessageStore.getRandomMsg(group);

        log.info("Messages are {} and {}", message_1.messageId(), message_2.messageId());
        log.info("Groups are {} and {}", message_1.groupId(), message_2.groupId());

        topicPartDataManager.addMessage(topicPartition, MessageStore.getRandomMsg(group)).toCompletableFuture().join();
        topicPartDataManager.addMessage(topicPartition, MessageStore.getRandomMsg(group)).toCompletableFuture().join();

        subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
    }

    @Test(invocationCount = 20)
    public void shouldIterateMessages_ForSameGroup_CheckConsistencyOverMultipleRuns() {
        PeekingIterator<MessageWithGroup> iterator = subscriberGroup.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(count, 2);
    }
}