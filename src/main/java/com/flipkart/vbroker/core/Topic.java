package com.flipkart.vbroker.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@Setter
public class Topic {
    public static final short DEFAULT_NO_OF_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 3;
    public static final String DEFAULT_TEAM = "DEFAULT";

    private short id;
    private short noOfPartitions = DEFAULT_NO_OF_PARTITIONS;
    private short replicationFactor = DEFAULT_REPLICATION_FACTOR;
    private boolean grouped = false;
    private String name;
    private String team = DEFAULT_TEAM;
    private TopicCategory topicCategory = TopicCategory.TOPIC;
    private List<TopicPartition> partitions;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Topic))
            return false;
        final Topic topic = (Topic) o;
        return id == topic.id;
    }

    public Topic(short id, short noOfPartitions, short replicationFactor, boolean grouped, String name, String team,
                 TopicCategory topicCategory) {
        super();
        this.id = id;
        this.noOfPartitions = noOfPartitions;
        this.replicationFactor = replicationFactor;
        this.grouped = grouped;
        this.name = name;
        this.team = team;
        this.topicCategory = topicCategory;
    }

    public enum TopicCategory {
        QUEUE, TOPIC
    }
}
