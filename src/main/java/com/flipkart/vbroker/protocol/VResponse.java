package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VResponse {
    private int correlationId;
    private int responseLength;
    private ByteBuf responsePayload;
}
