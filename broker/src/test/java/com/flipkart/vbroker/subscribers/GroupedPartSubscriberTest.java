package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
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
}
