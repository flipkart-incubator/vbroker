package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.AllArgsConstructor;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 2/2/18
 */

@AllArgsConstructor
public class SubscriberMetadataService {
    private final SubscriptionService subscriptionService;
    private final TopicService topicService;
    private final TopicPartDataManager topicPartDataManager;

    public void saveSubscriptionMetadata(Subscription subscription) throws IOException {
        Topic topic = topicService.getTopic(subscription.topicId()).toCompletableFuture().join();
        //Save each group's file as : metadata/{topicID}/subs/{subID}/{partitionID}/{groupID}.txt
        for (PartSubscription partSubscription : SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions())) {
            PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription).toCompletableFuture().join();
            File dir = new File(getPartSubscriberPath(partSubscriber));
            dir.mkdirs();
            for (SubscriberGroup subscriberGroup : partSubscriber.getSubscriberGroupsMap().values()) {
                File tmp = new File(dir, subscriberGroup.getGroupId().concat(".txt"));
                tmp.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmp));
                bufferedWriter.write(subscriberGroup.getQType().toString());
                bufferedWriter.newLine();
                bufferedWriter.write(subscriberGroup.getCurrSeqNo().toString());
                bufferedWriter.close();
            }
        }
    }

    public void loadSubscriptionMetadata(Subscription subscription) {
        Topic topic = topicService.getTopic(subscription.topicId()).toCompletableFuture().join();
        for (PartSubscription partSubscription : SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions())) {
            PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription).toCompletableFuture().join();
            TopicPartition partition = partSubscription.getTopicPartition();
            File dir = new File(getPartSubscriberPath(partSubscriber));
            for (String groupId : topicPartDataManager.getUniqueGroups(partition)) {
                MessageGroup messageGroup = new MessageGroup(groupId, partition);
                File subscriberGroupFile = new File(dir, groupId.concat(".txt"));
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, partSubscription, topicPartDataManager);
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(subscriberGroupFile));
                    SubscriberGroup.QType qType = SubscriberGroup.QType.valueOf(reader.readLine());
                    AtomicInteger seqNo = new AtomicInteger(Integer.parseInt(reader.readLine()));
                    subscriberGroup.setCurrSeqNo(seqNo);
                    subscriberGroup.setQType(qType);
                } catch (IOException ignored) {
                } finally {
                    partSubscriber.getSubscriberGroupsMap().put(groupId, subscriberGroup);
                }
            }

        }
    }

    public void saveAllSubscribers() throws IOException {
        for (Subscription subscription : subscriptionService.getAllSubscriptions().toCompletableFuture().join()) {
            saveSubscriptionMetadata(subscription);
        }
    }

    private String getPartSubscriberPath(PartSubscriber partSubscriber) {
        short topicId = partSubscriber.getPartSubscription().getTopicPartition().getTopicId();
        short partitionId = partSubscriber.getPartSubscription().getTopicPartition().getId();
        short subscriptionId = partSubscriber.getPartSubscription().getSubscriptionId();
        return (new StringBuilder()).append("metadata/").append(topicId).append("/subs/").append(subscriptionId).append("/").append(partitionId).toString();

    }
}