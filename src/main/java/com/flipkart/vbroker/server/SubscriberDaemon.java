package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchRequest;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.protocol.Request;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@AllArgsConstructor
public class SubscriberDaemon implements Runnable {

    private final LocalAddress address;
    private final Bootstrap consumerBootstrap;

    @Override
    public void run() {
        log.info("Subscriber now running");

        while (true) {
            try {
                long timeMs = 1000;
                log.info("Sleeping for {} milli secs before connecting to server", timeMs);
                Thread.sleep(timeMs);

                log.info("Subscriber connecting to server at address {}", address);
                Channel consumerChannel = consumerBootstrap.connect(address).sync().channel();
                log.info("Subscriber connected to local server address {}", address);

                FlatBufferBuilder builder = new FlatBufferBuilder();
                int fetchRequest = FetchRequest.createFetchRequest(builder,
                        (short) 11,
                        (short) 1,
                        (short) 1);
                int vRequest = VRequest.createVRequest(builder,
                        (byte) 1,
                        1001,
                        RequestMessage.FetchRequest,
                        fetchRequest);
                builder.finish(vRequest);
                ByteBuffer byteBuffer = builder.dataBuffer();
                ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                Request request = new Request(byteBuf.readableBytes(), byteBuf);

                log.info("Sending FetchRequest to broker");
                consumerChannel.writeAndFlush(request);

                consumerChannel.closeFuture().sync();
                break;
            } catch (InterruptedException e) {
                log.error("Exception in consumer in connecting to the broker", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.info("== Subscriber shutdown ==");
    }
}
