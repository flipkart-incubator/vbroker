package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Request {
    private final int requestLength;
    private final ByteBuf vRequest;
}
