package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kaushal.hooda on 27/02/18.
 */
public class InMemoryGroupedSubPartDataTest {
    private SubPartData subPartData;

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @Test
    public void shouldGetLag() throws Exception {
        PartSubscription partSubscription = mock(PartSubscription.class);
        TopicPartition topicPartition = mock(TopicPartition.class);
        when(topicPartition.getId()).thenReturn((short) 1);
        when(topicPartition.getTopicId()).thenReturn((short) 1);

        SubscriberGroup group1 = setupMockGroup("g-1", topicPartition, 0);
        SubscriberGroup group2 = setupMockGroup("g-2", topicPartition, 2);
        SubscriberGroup group3 = setupMockGroup("g-3", topicPartition, 3);

        subPartData = new InMemoryGroupedSubPartData(partSubscription);
        subPartData.addGroup(group1).toCompletableFuture().join();
        subPartData.addGroup(group2).toCompletableFuture().join();
        subPartData.addGroup(group3).toCompletableFuture().join();

        int lag = subPartData.getLag().toCompletableFuture().join();
        Assert.assertEquals(lag, 5);
    }

    @SuppressWarnings("unchecked")
    private SubscriberGroup setupMockGroup(String groupId, TopicPartition topicPartition, int lag) {
        SubscriberGroup group = mock(SubscriberGroup.class);
        when(group.getGroupId()).thenReturn(groupId);
        when(group.getLag()).thenReturn(CompletableFuture.completedFuture(lag));
        when(group.getTopicPartition()).thenReturn(topicPartition);
        when(group.iterator()).thenReturn(mock(PeekingIterator.class));
        return group;
    }

}