package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Response {
    private final int responseLength;
    private final ByteBuf vResponse;
}
