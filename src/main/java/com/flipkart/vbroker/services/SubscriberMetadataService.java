package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.*;
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
    private final TopicPartitionDataManager topicPartitionDataManager;

    public void saveSubscriptionMetadata(Subscription subscription) throws IOException {
        //Save each group's file as : metadata/{topicID}/subs/{subID}/{partitionID}/{groupID}.txt
        for (PartSubscription partSubscription : subscription.getPartSubscriptions()) {
            PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription);
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
        for (PartSubscription partSubscription : subscription.getPartSubscriptions()) {
            PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription);
            TopicPartition partition = partSubscription.getTopicPartition();
            File dir = new File(getPartSubscriberPath(partSubscriber));
            for (String groupId : topicPartitionDataManager.getUniqueGroups(partition)) {
                MessageGroup messageGroup = topicPartitionDataManager.getMessageGroup(partition, groupId).get();
                File subscriberGroupFile = new File(dir, groupId.concat(".txt"));
                SubscriberGroup subscriberGroup = SubscriberGroup.newGroup(messageGroup, topicPartitionDataManager);
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
        for (Subscription subscription : subscriptionService.getAllSubscriptions()) {
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