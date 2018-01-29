package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"groupIdMessageGroupMap"})
@ToString
public class TopicPartition {
    private final Map<String, MessageGroup> groupIdMessageGroupMap = MemoryManager.getCapacityManagedMap(new SampleEvictionStrategy(), new SampleL3Provider());
    private final short id;
    private final short topicId;

    public TopicPartition(short id, short topicId) {
        this.id = id;
        this.topicId = topicId;
    }

    public void addMessage(Message message) {
        String groupId = message.groupId();
        groupIdMessageGroupMap.computeIfAbsent(groupId, s -> new MessageGroup(groupId, groupIdMessageGroupMap)).appendMessage(message);
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
