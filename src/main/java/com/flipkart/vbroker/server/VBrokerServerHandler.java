package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Response;
import com.flipkart.vbroker.protocol.VResponseEncoder;
import com.google.common.base.Charsets;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class VBrokerServerHandler extends SimpleChannelInboundHandler<VRequest> {

    private Channel outboundChannel;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest request) throws Exception {
        log.info("== ChannelRead0 ==");
        log.info("== Received VRequest {} with correlationId {} and type {} ==", request, request.correlationId(), request.requestMessageType());

        switch (request.requestMessageType()) {
            case RequestMessage.ProduceRequest:
                log.info("Request is of type ProduceRequest");
                ProduceRequest produceRequest = (ProduceRequest) request.requestMessage(new ProduceRequest());
                assert produceRequest != null;
                log.info("Getting messageSet for topic {} and partition {}", produceRequest.topicId(), produceRequest.partitionId());
                MessageSet messageSet = produceRequest.messageSet();
                for (int i = 0; i < messageSet.messagesLength(); i++) {
                    Message message = messageSet.messages(i);
                    ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
                    ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                    log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                            Charsets.UTF_8.decode(byteBuffer).toString());

                    // Prepare the HTTP request.
                    FullHttpRequest httpRequest = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1,
                            io.netty.handler.codec.http.HttpMethod.POST,
                            requireNonNull(message.httpUri()),
                            byteBuf);
                    httpRequest.headers().set(HttpHeaderNames.HOST, "localhost");
                    httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    httpRequest.headers().set("Content-Type", "application/json");
                    httpRequest.headers().set(MessageConstants.MESSAGE_ID_HEADER, message.messageId());
                    httpRequest.headers().set(MessageConstants.GROUP_ID_HEADER, message.groupId());

                    log.info("Making httpRequest to httpUri: {} and httpMethod: {}",
                            message.httpUri(),
                            HttpMethod.name(message.httpMethod()));

                    //ctx.channel().writeAndFlush(httpRequest);
                    //ctx.writeAndFlush(httpRequest);
                    if (outboundChannel == null) {
                        createOutboundChannel(ctx, httpRequest);
                    }
                }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    private void createOutboundChannel(ChannelHandlerContext gCtx,
                                       FullHttpRequest httpRequest) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(gCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new VResponseEncoder());
                        //pipeline.addLast(new VBrokerServerResponseHandler());
                        pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                                if (msg instanceof HttpResponse) {

                                    HttpResponse httpResponse = (HttpResponse) msg;
                                    log.info("== Got HttpResponse with status {} ==", httpResponse.status());

                                }

                                if (msg instanceof HttpContent) {
                                    HttpContent content = (HttpContent) msg;
                                    System.err.print(content.content().toString(CharsetUtil.UTF_8));
                                    System.err.flush();

                                    if (content instanceof LastHttpContent) {
                                        System.err.println("} END OF CONTENT");

                                        short topicId = 101;
                                        short partitionId = 0;
                                        int correlationId = 1001;

                                        FlatBufferBuilder builder = new FlatBufferBuilder();
                                        int produceResponse = ProduceResponse.createProduceResponse(
                                                builder,
                                                topicId,
                                                partitionId,
                                                (short) 200);
                                        int vResponse = VResponse.createVResponse(
                                                builder,
                                                correlationId,
                                                ResponseMessage.ProduceResponse,
                                                produceResponse);
                                        builder.finish(vResponse);
                                        ByteBuffer responseByteBuffer = builder.dataBuffer();
                                        ByteBuf byteBuf = Unpooled.wrappedBuffer(responseByteBuffer);

                                        log.info("Writing Response to gCtx");
                                        Response response = new Response(byteBuf.readableBytes(), byteBuf);
                                        gCtx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                        log.info("Done writing Response to gCtx");
                                        ctx.close();
                                    }
                                }
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 12000);
        channelFuture.addListener((ChannelFutureListener) future -> {
            log.info("channel creation to dest client is {}", future.isSuccess());
            if (!future.isSuccess()) {
                gCtx.channel().close();
            }

            outboundChannel = future.channel();
            outboundChannel.writeAndFlush(httpRequest);
            log.info("Wrote httpRequest");
            log.info("Handler-> Created outboundChannel to {}", future.channel().localAddress());
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in server handling", cause);
        ctx.close();
    }
}
