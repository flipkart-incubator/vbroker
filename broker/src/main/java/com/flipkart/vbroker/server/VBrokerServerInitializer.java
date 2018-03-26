package com.flipkart.vbroker.server;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.handlers.RequestHandlerFactory;
import com.flipkart.vbroker.handlers.VBrokerRequestHandler;
import com.flipkart.vbroker.protocol.codecs.VBrokerServerCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerServerInitializer extends ChannelInitializer<Channel> {

    private final RequestHandlerFactory requestHandlerFactory;
    private final MetricRegistry metricRegistry;

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new VBrokerServerCodec());
        pipeline.addLast(new VBrokerRequestHandler(requestHandlerFactory, metricRegistry));
    }
}
