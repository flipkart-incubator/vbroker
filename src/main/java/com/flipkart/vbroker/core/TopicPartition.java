package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@EqualsAndHashCode(exclude = {"groupIdMessageGroupMap"})
@ToString
public class TopicPartition {
    private final ConcurrentMap<String, MessageGroup> groupIdMessageGroupMap = new ConcurrentHashMap<>();
    private final short id;
    private final short topicId;

    public TopicPartition(short id, short topicId) {
        this.id = id;
        this.topicId = topicId;
    }

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
