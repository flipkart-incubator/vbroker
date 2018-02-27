package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerClientInitializer;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.google.flatbuffers.FlatBufferBuilder;
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

import java.nio.ByteBuffer;

public class TopicCreateTest {

    @Test
    public void testTopicCreate() throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new TopicCreateClientInitializer());

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

        FlatBufferBuilder builder = new FlatBufferBuilder();

        int topicName = builder.createString("topic201");

        int topic = Topic.createTopic(builder, (short) 201, topicName, true, (short) 1, (short) 3, TopicCategory.TOPIC);
        int topicCreateRequest = TopicCreateRequest.createTopicCreateRequest(builder, topic);
        int vRequest = VRequest.createVRequest(builder, (byte) 1, 1002, RequestMessage.CreateTopicsRequest,
            topicCreateRequest);
        builder.finish(vRequest);

        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
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