package com.flipkart.vbroker.client;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class ProducerRecord {
    private String messageId;
    private String groupId;
    private byte crc;
    private byte version;
    private int seqNo;
    private short topicId;
    private short partitionId;
    private int attributes;
    private String httpUri;
    private byte httpMethod;
    private short callbackTopicId;
    private String callbackHttpUri;
    private byte callbackHttpMethod;
    private Map<String, String> headers;
    private byte[] payload;
}
