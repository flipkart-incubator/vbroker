package com.flipkart.vbroker.core;

import lombok.ToString;

@ToString
public class Topic {

    public static final int DEFAULT_PARTITIONS = 1;
    public static final int DEFAULT_REPLICATION_FACTOR = 3;
    public static final String DEFAULT_TEAM = "default";

    private String team = DEFAULT_TEAM;
    private String name;
    private boolean grouped = false;
    private int noOfPartitions = DEFAULT_PARTITIONS;
    private int replicationFactor = DEFAULT_REPLICATION_FACTOR;

    public Topic(String team, String name, boolean grouped, int noOfPartitions, int replicationFactor) {
        super();
        this.team = team;
        this.name = name;
        this.grouped = grouped;
        this.noOfPartitions = noOfPartitions;
        this.replicationFactor = replicationFactor;
    }

}
