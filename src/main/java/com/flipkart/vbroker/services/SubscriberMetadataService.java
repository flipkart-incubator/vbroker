package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.SubscriberGroup;
import com.flipkart.vbroker.core.Subscription;
import lombok.AllArgsConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by hooda on 2/2/18
 */

@AllArgsConstructor
public class SubscriberMetadataService {
    private final SubscriptionService subscriptionService;
    private final TopicService topicService;

    public void saveSubscriptionMetadata(Subscription subscription) throws IOException {
        //Save each group's file as : metadata/{topicID}/subs/{subID}/{partitionID}/{groupID}.txt
        for(PartSubscription partSubscription : subscription.getPartSubscriptions()) {
            PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription);
            File dir = new File(getPartSubscriberPath(partSubscriber));
            dir.mkdirs();
            for(SubscriberGroup subscriberGroup : partSubscriber.getSubscriberGroupsMap().values()){
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

    public void saveAllSubscribers() throws IOException {
        for(Subscription subscription : subscriptionService.getAllSubscriptions()){
            saveSubscriptionMetadata(subscription);
        }

    }

    private String getPartSubscriberPath(PartSubscriber partSubscriber){
        short topicId = partSubscriber.getPartSubscription().getTopicPartition().getTopicId();
        short partitionId = partSubscriber.getPartSubscription().getTopicPartition().getId();
        short subscriptionId = partSubscriber.getPartSubscription().getSubscriptionId();
        return (new StringBuilder()).append("metadata/").append(topicId).append("/subs/").append(subscriptionId).append("/").append(partitionId).toString();

    }
}