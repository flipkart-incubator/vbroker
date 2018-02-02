package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Subscriber {

    private final String subscriberId;
    private final List<PartSubscriber> partSubscribers = new ArrayList<>();

    public Subscriber(String subscriberId, List<PartSubscriber> partSubscribers) {
        this.subscriberId = subscriberId;
        this.partSubscribers.addAll(partSubscribers);
    }

    /**
     * set the seqNo of the given group
     *
     * @param group to set seqNo for
     * @param seqNo to set the group position to
     */
    public void setSeqNo(TopicPartition topicPartition, String group, int seqNo) {

    }

    private void process(Message message) {

    }
}
