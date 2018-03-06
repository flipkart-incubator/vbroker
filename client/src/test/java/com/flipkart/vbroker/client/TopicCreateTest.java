package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.utils.FlatbufUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.testng.annotations.Test;

public class TopicCreateTest {

    @Test
    public void testTopicCreate() throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new TopicCreateClientInitializer());

        VBClientConfig config = VBClientConfig.newConfig("client.properties");

        Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

        ProtoTopic topic201 = ProtoTopic.newBuilder().setName("topic201").setGrouped(true).setPartitions(1).setReplicationFactor(3).setTopicCategory(TopicCategory.TOPIC).build();
        CreateTopicsRequest createTopicsRequest = CreateTopicsRequest.newBuilder().addTopics(topic201).build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setCreateTopicsRequest(createTopicsRequest).build();
        VRequest vRequest = FlatbufUtils.createVRequest((byte) 1, 1002, protoRequest);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(vRequest.getByteBuffer());
        Request request = new Request(byteBuf.readableBytes(), byteBuf);
        channel.writeAndFlush(request);
        channel.closeFuture().sync();
    }

    class TopicCreateClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new VBrokerClientCodec());
            ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);
            pipeline.addLast(new VBrokerClientInitializer(responseHandlerFactory));

        }
    }

}
