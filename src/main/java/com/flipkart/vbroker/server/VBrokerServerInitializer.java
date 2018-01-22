package com.flipkart.vbroker.server;

import com.flipkart.vbroker.protocol.codecs.VBrokerServerCodec;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerServerInitializer extends ChannelInitializer<Channel> {

    private final RequestHandlerFactory requestHandlerFactory;

    private CuratorService curatorService;

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new VBrokerServerCodec());
        TopicService topicService = new TopicService(curatorService);
        pipeline.addLast(new VBrokerServerHandler(topicService, requestHandlerFactory));
    }
}
