package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import org.testng.annotations.BeforeMethod;

public class GroupedPartSubscriberTest extends BasePartSubscriberTest {

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

    @Override
    public void shouldIterateOver_NewMessages_SameGroup() throws InterruptedException {
        super.shouldIterateOver_NewMessages_SameGroup();
    }

    @Override
    public void shouldIterateOver_NewMessages_DiffGroups() throws InterruptedException {
        super.shouldIterateOver_NewMessages_DiffGroups();
    }

    @Override
    public void shouldIterateOver_SidelinedMessages() throws InterruptedException {
        super.shouldIterateOver_SidelinedMessages();
    }

    @Override
    public void shouldIterateOver_RetryMessages_MainQToRQ1() throws InterruptedException {
        super.shouldIterateOver_RetryMessages_MainQToRQ1();
    }

    @Override
    public void shouldIterateOver_RetryMessages_RQ1ToRQ2() throws InterruptedException {
        super.shouldIterateOver_RetryMessages_RQ1ToRQ2();
    }

    @Override
    public void shouldIterateOver_RetryMessages_RQ2ToRQ3() throws InterruptedException {
        super.shouldIterateOver_RetryMessages_RQ2ToRQ3();
    }

    @Override
    public void shouldIterateOver_MoveFromRQ3ToSQ() throws InterruptedException {
        super.shouldIterateOver_MoveFromRQ3ToSQ();
    }
}
