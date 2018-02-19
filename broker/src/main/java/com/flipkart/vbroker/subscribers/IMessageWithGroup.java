package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.entities.Message;

public interface IMessageWithGroup {

    Message getMessage();

    short subscriptionId();

    short getTopicId();

    void sideline();

    void retry();

    boolean isLocked();

    boolean lock();

    void unlock();
}
