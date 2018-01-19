package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

public class SubscriptionGroup {
    private short topicId;
    MessageGroup messageGroup;
    private QType qType;
    private int currSeqNo;

    public List<Message> getUnconsumedMessages(int count){
        return messageGroup.getMessages().subList(currSeqNo, currSeqNo + count);
    }

    public enum QType{
        MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3
    }

}
