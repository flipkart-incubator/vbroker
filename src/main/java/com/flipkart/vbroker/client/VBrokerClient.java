package com.flipkart.vbroker.client;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.protocol.VRequest;
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
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new VBrokerClientInitializer());

            Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();
//            channel.writeAndFlush((short) 101);

            VRequest request = new VRequest();
            request.setVersion((short) 1);
            request.setApiKey((short) 101);

            ByteBuffer byteBuffer = VBrokerSampleEncoder.encodeSampleMsg();
            ByteBuf byteBuf = Unpooled.copiedBuffer(byteBuffer);

            request.setRequestLength(byteBuf.readableBytes());
            request.setRequestPayload(byteBuf);

            channel.writeAndFlush(request);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
