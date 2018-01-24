package com.flipkart.vbroker.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
public class Topic {
    public static final short DEFAULT_NO_OF_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 3;

    private final short id;
    @Setter
    private short noOfPartitions = DEFAULT_NO_OF_PARTITIONS;
    @Setter
    private short replicationFactor = DEFAULT_REPLICATION_FACTOR;
    @Setter
    private boolean grouped = false;
    @Setter
    private TopicCategory topicCategory = TopicCategory.TOPIC;
    private final List<TopicPartition> partitions = new ArrayList<>();

    public Topic(Short id) {
        this.id = id;
    }

    //make it package access
    public void addPartition(TopicPartition topicPartition) {
        this.partitions.add(topicPartition);
    }

    public TopicPartition getPartition(short id) {
        return partitions.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        final Topic topic = (Topic) o;
        return id == topic.id;
    }

    public enum TopicCategory {
        QUEUE, TOPIC
    }
}
