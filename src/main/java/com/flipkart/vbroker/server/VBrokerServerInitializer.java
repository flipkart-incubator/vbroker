package com.flipkart.vbroker.server;

import com.flipkart.vbroker.protocol.codecs.VBrokerServerCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new VBrokerServerCodec());
        pipeline.addLast(new VBrokerServerHandler());
    }
}
