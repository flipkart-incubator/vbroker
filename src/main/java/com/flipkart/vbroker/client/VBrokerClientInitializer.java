package com.flipkart.vbroker.client;

import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.server.ResponseHandlerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerClientInitializer extends ChannelInitializer<SocketChannel> {

    private final ResponseHandlerFactory responseHandlerFactory;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new VBrokerClientCodec());
        pipeline.addLast(new VBrokerResponseHandler(responseHandlerFactory));
    }
}
