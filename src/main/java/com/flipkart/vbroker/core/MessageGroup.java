package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

@Getter
@Setter
public class MessageGroup {
    private String groupId;
    private List<Message> messages = new LinkedList<>();
    private Level level;

    public enum Level {
        L1, L2, L3
    }

    public void appendMessage(Message message) {
        this.messages.add(message);
    }
}
