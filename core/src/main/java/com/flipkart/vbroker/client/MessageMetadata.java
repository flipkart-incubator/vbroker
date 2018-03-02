package com.flipkart.vbroker.client;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MessageMetadata {
    private final String messageId;
    private final short topicId;
    private final short partitionId;
    private final int memoryLocation; //dummy
}
