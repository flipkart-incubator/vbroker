package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.MessageSet;
import com.flipkart.vbroker.entities.ProduceRequest;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.server.ResponseHandlerFactory;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class VBrokerClient {

    public static void main(String args[]) throws InterruptedException, IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: ", config);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new VBrokerClientInitializer(responseHandlerFactory));

            Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

            FlatBufferBuilder builder = new FlatBufferBuilder();
            int[] messages = new int[1];
            messages[0] = MessageStore.getSampleMsg(builder);
            int messagesVector = MessageSet.createMessagesVector(builder, messages);
            int messageSet = MessageSet.createMessageSet(builder, messagesVector);
            int produceRequest = ProduceRequest.createProduceRequest(builder,
                    (short) 11,
                    (short) 1,
                    (short) 1,
                    messageSet);
            int vRequest = VRequest.createVRequest(builder,
                    (byte) 1,
                    1001,
                    RequestMessage.ProduceRequest,
                    produceRequest);
            builder.finish(vRequest);
            ByteBuffer byteBuffer = builder.dataBuffer();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);

            Request request = new Request(byteBuf.readableBytes(), byteBuf);
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
