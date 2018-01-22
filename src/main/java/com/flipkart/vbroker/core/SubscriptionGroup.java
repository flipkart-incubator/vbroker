package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;

import java.util.Iterator;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

public class SubscriptionGroup implements Iterable<Message> {
    private short topicId;
    MessageGroup messageGroup;
    private QType qType;
    private int currSeqNo;

    public List<Message> getUnconsumedMessages(int count) {
        return messageGroup.getMessages().subList(currSeqNo, currSeqNo + count);
    }

    @Override
    public Iterator<Message> iterator() {
        return null;
    }

    public enum QType {
        MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3
    }

    public void sidelineGroup() {
        this.qType = QType.SIDELINE;
    }

    public void setQType(QType qType) {
        this.qType = qType;
    }
}
