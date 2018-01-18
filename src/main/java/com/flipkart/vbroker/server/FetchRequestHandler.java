package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchRequest;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FetchRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final FetchRequest fetchRequest;

    @Override
    public void handle() {
        log.info("Handling FetchRequest");
    }
}
