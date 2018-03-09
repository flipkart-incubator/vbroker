package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;

@Slf4j
public class UnGroupedPartSubscriberTest extends BasePartSubscriberTest {

    @BeforeMethod
    @Override
    public void setUp() {
        PartSubscription partSubscription = SubscriptionUtils.getPartSubscription(DummyEntities.unGroupedSubscription, (short) 0);
        InMemoryTopicPartDataManager topicPartDataManager = new InMemoryTopicPartDataManager();
        InMemorySubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);


        subPartDataManager.getIterator(partSubscription, QType.MAIN);

        setPartSubscription(partSubscription);
        setTopicPartDataManager(topicPartDataManager);
        setSubPartDataManager(subPartDataManager);

        setPartSubscriber(new UnGroupedPartSubscriber(subPartDataManager, partSubscription));
    }

    @Override
    public void shouldIterateOver_NewMessages() throws InterruptedException {
        super.shouldIterateOver_NewMessages();
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