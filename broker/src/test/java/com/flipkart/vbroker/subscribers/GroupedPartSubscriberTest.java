package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import org.testng.annotations.BeforeMethod;

public class GroupedPartSubscriberTest extends BasePartSubscriberTest {
    @Override
    public void shouldIterateOver_NewMessages() throws InterruptedException {
        super.shouldIterateOver_NewMessages();
    }

    @Override
    public void shouldIterateOver_SidelinedMessages() throws InterruptedException {
        super.shouldIterateOver_SidelinedMessages();
    }

    @Override
    public void shouldIterateOver_RetryMessages() throws InterruptedException {
        super.shouldIterateOver_RetryMessages();
    }

    @Override
    public void shouldIterateOver_MoveFromRQ3ToSQ() throws InterruptedException {
        super.shouldIterateOver_MoveFromRQ3ToSQ();
    }

    @BeforeMethod
    @Override
    public void setUp() {
        PartSubscription partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.groupedSubscription, (short) 0);
        InMemoryTopicPartDataManager topicPartDataManager = new InMemoryTopicPartDataManager();
        InMemorySubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);

        setPartSubscription(partSubscription);
        setTopicPartDataManager(topicPartDataManager);
        setSubPartDataManager(subPartDataManager);

        setPartSubscriber(new GroupedPartSubscriber(topicPartDataManager, subPartDataManager, partSubscription));
    }
}
