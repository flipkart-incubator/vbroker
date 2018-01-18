package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.services.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerConsumerHandler extends SimpleChannelInboundHandler<Message> {

    private final MessageProcessor messageProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        messageProcessor.process(msg);
    }
}
