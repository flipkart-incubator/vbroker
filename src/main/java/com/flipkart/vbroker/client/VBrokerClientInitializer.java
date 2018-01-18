package com.flipkart.vbroker.client;

import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        //pipeline.addLast(new CombinedBytesShortCodec());

        pipeline.addLast(new VBrokerClientCodec());
        pipeline.addLast(new VBrokerClientHandler());
    }
}
