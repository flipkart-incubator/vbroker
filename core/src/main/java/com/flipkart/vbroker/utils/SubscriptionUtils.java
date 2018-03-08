package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.wrappers.Subscription;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionUtils {

    /**
     * Utility method to create new PartSubscription entity.
     *
     * @param subscription subscription entity
     * @param partitionId  partitionId of the topic, to which the part-subscription is
     *                     subscribed to.
     * @return
     */
    public static PartSubscription getPartSubscription(Subscription subscription, int partitionId) {
        return new PartSubscription(partitionId, new TopicPartition(partitionId, subscription.topicId(), subscription.grouped()),
            subscription.id(), subscription.grouped());
    }

    /**
     * Return list of PartSubscriptions for given subscription and no of
     * partitions.
     *
     * @param subscription
     * @param partitions
     * @return
     */
    public static List<PartSubscription> getPartSubscriptions(Subscription subscription, int partitions) {
        //TODO: this shouldn't be a static method - move this to SubscriptionService
        List<PartSubscription> partSubscriptions = new ArrayList<>();
        for (short i = 0; i < partitions; i++) {
            partSubscriptions
                .add(new PartSubscription(i, new TopicPartition(i, subscription.topicId(), subscription.grouped()),
                    subscription.id(), subscription.grouped()));
        }
        return partSubscriptions;
    }

}
