package com.flipkart.vbroker.client;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.util.Map;

@Builder
@Getter
@ToString
public class ConsumerRecord {
    private String messageId;
    private String groupId;
    private byte crc;
    private byte version;
    private int seqNo;
    private int topicId;
    private int attributes;
    private Map<String, String> headers;
    private ByteBuffer payload;
}
