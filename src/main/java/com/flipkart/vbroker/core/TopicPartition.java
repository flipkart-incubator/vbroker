package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@Setter
public class TopicPartition {
    private short id;
    private List<MessageGroup> messageGroups;

    private MessageGroup getMessageGroup(String groupId) {
        throw new VBrokerException("Not implemented yet");
    }

    public synchronized void addMessage(Message message) {
        String groupId = message.groupId();
        MessageGroup messageGroup = getMessageGroup(groupId);
        messageGroup.getMessages().add(message);
    }

    public List<MessageGroup> getMessageGroups() {
        return messageGroups;
    }
}
