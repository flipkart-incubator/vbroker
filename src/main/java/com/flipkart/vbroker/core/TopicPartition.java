package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@Setter
public class TopicPartition {
    private short id;

    public TopicPartition(short id) {
        this.id = id;
    }

    private final ConcurrentMap<String, MessageGroup> groupIdMessageGroupMap = new ConcurrentHashMap<>();

    public void addMessage(Message message) {
        String groupId = message.groupId();
        groupIdMessageGroupMap.computeIfAbsent(groupId, s -> {
            MessageGroup newGroup = new MessageGroup(groupId);
            newGroup.appendMessage(message);
            return newGroup;
        });
    }

    public List<MessageGroup> getMessageGroups() {
        return new ArrayList<>(groupIdMessageGroupMap.values());
    }

    public Optional<MessageGroup> getMessageGroup(String groupId) {
        return Optional.ofNullable(groupIdMessageGroupMap.get(groupId));
    }

    public Set<String> getUniqueGroups() {
        return groupIdMessageGroupMap.keySet();
    }
}
