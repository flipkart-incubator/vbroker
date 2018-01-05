package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.exceptions.VBrokerException;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class VRequest {

    private short version;
    private ApiKey apiKey;
    private int correlationId;
    private int requestLength;
    private ByteBuf requestPayload;

    @AllArgsConstructor
    @Getter
    public enum ApiKey {
        PRODUCE_REQUEST((short) 0),
        PRODUCE_RESPONSE((short) 1),
        TOPIC_CREATE_REQUEST((short) 2),
        TOPIC_CREATE_RESPONSE((short) 3);

        private short value;

        public static ApiKey getKey(short value) {
            ApiKey[] values = ApiKey.values();
            if (values.length - 1 < value) {
                throw new VBrokerException(String.format("Unknown ApiKey short: %d", value));
            }
            return values[value];
        }
    }
}
