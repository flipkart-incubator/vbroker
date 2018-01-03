package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class VRequest {
    private short version;
    private short apiKey;
    private Integer requestLength;
    private ByteBuf requestPayload;
}
