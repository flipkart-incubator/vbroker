package com.flipkart.vbroker.client;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@ToString
public class ProducerRecord {
    private String messageId;
    private String groupId;
    private byte crc;
    private byte version;
    private int seqNo;
    private short topicId;
    private int attributes;
    private String httpUri;
    private HttpMethod httpMethod;
    private short callbackTopicId;
    private String callbackHttpUri;
    private HttpMethod callbackHttpMethod;
    private Map<String, String> headers;
    private byte[] payload;

    public enum HttpMethod {
        POST(0), PUT(1), DELETE(2), PATCH(3);

        private final int idx;

        HttpMethod(int idx) {
            this.idx = idx;
        }

        public byte idx() {
            return (byte) idx;
        }
    }
}
