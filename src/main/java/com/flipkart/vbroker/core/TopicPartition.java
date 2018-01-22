package com.flipkart.vbroker.core;

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
}
