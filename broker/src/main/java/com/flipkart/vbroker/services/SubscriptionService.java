package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.wrappers.Subscription;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface SubscriptionService {

    /**
     * Create subscription request. This will only persist the data in coordinator, which will be picked up by controller.
     *
     * @param subscription subscription data to be created
     * @return
     */
    public CompletionStage<Subscription> createSubscription(Subscription subscription);

    /**
     * Create subscription entity. This is invoked by the controller to create subscription entity.
     *
     * @param id           id with which subscription should be created
     * @param subscription subscription entity to create
     * @return
     */
    public CompletionStage<Subscription> createSubscriptionAdmin(short id, Subscription subscription);

    /**
     * @param topicId        id of the topic to which the subscription belongs
     * @param subscriptionId id of the subscription to fetch
     * @return
     */
    public CompletionStage<Subscription> getSubscription(int topicId, int subscriptionId);

    /**
     * Gets all subscriptions across all topics
     *
     * @return
     */
    public CompletionStage<List<Subscription>> getAllSubscriptions();

    /**
     * Gets all subscriptions for a particular topicId
     *
     * @param topicId id of the topic for which all subscriptions are to be fetched
     * @return
     */
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(int topicId);

    /**
     * Gets all subscriptions for a broker.
     *
     * @param brokerId id of the broker for which all subscriptions are to be fetched.
     * @return
     */
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId);

    /**
     * Gets PartSubscription entity for the given subscription and partitionId.
     *
     * @param subscription
     * @param partSubscriptionId
     * @return
     */
    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, int partSubscriptionId);

    /**
     * Gets PartSubscriber for the given PartSubscription.
     *
     * @param subscription
     * @return
     */
    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription subscription);

    /**
     * Gets list of PartSubscriptions for given subscription.
     *
     * @param subscription
     * @return
     */
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription);

    public CompletionStage<Integer> getPartSubscriptionLag(PartSubscription partSubscription);
}
