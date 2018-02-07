package com.flipkart.vbroker.core;

import com.flipkart.vbroker.data.LocalMemoryManager;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.ForwardingList;
import lombok.Getter;

import java.util.*;

@Getter
public class MessageList<Item> extends ForwardingList {
    private final static Map<TopicPartitionGroupId, MessageList> multitonMap = new HashMap<TopicPartitionGroupId, MessageList>();
    private List<Item> itemList;
    private String groupId;
    private short partitionId;
    private short topicId;
    private Level level;
    private ObservableVList observableVList;

    private MessageList() {

    }

    private MessageList(String groupId, short partitionId, short topicId) {
        this.groupId = groupId;
        this.partitionId = partitionId;
        this.topicId = topicId;
        this.level = Level.L2;
        observableVList = new ObservableVList();
        itemList = new LinkedList<>();
    }

    public static MessageList getInstance(String groupId, short partitionId, short topicId) {
        TopicPartitionGroupId topicPartitionGroupId = new TopicPartitionGroupId(new TopicPartitionId(partitionId, topicId), groupId);
        MessageList instance = multitonMap.get(topicPartitionGroupId);
        if (instance == null) {
            synchronized (MessageList.class) {
                instance = new MessageList(groupId, partitionId, topicId);
                multitonMap.put(topicPartitionGroupId, instance);
            }
        }
        return instance;
    }

    @Override
    public boolean add(Object o) {
        boolean b = itemList.add((Item) o);
        if (b && o.getClass() == Message.class) {
            observableVList.setChanged();
            observableVList.notifyObservers(((Message) o).bodyLength() + ((Message) o).headersLength());
        }
        return b;
    }

    @Override
    protected List delegate() {
        return itemList;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void addObserver(LocalMemoryManager localMemoryManager) {
        observableVList.addObserver(localMemoryManager);
    }

    protected enum Level {
        L1, L2, L3
    }

    public class ObservableVList extends Observable {
        public void setChanged() {
            super.setChanged();
        }

        public void notifyObservers(int listUsedCapacity) {
            super.notifyObservers(listUsedCapacity);
        }

        public void setLevel(Level level) {
            MessageList.this.setLevel(level);
        }

        public MessageList getVList() {
            return MessageList.this;
        }

        public String getGroupId() {
            return MessageList.this.groupId;
        }

        public short getPartitionId() {
            return MessageList.this.partitionId;
        }

        public short getTopicId() {
            return MessageList.this.topicId;
        }
    }
}
