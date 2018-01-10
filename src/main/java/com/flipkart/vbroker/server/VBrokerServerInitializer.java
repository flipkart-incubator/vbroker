package com.flipkart.vbroker.server;

import com.flipkart.vbroker.controller.CuratorService;
import com.flipkart.vbroker.controller.TopicService;
import com.flipkart.vbroker.protocol.codecs.VBrokerServerCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerServerInitializer extends ChannelInitializer<SocketChannel> {

    private CuratorService curatorService;

    public VBrokerServerInitializer(CuratorService curatorService) {
        super();
        this.curatorService = curatorService;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new VBrokerServerCodec());
        TopicService topicService = new TopicService(curatorService);
        pipeline.addLast(new VBrokerServerHandler(topicService));
    }
}
