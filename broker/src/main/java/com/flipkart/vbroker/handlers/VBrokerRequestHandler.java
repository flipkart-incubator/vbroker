package com.flipkart.vbroker.handlers;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.vbroker.flatbuf.RequestMessage;
import com.flipkart.vbroker.flatbuf.ResponseMessage;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.protocol.Response;
import com.flipkart.vbroker.utils.MetricUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerRequestHandler extends SimpleChannelInboundHandler<VRequest> {

    private final RequestHandlerFactory requestHandlerFactory;
    private final MetricRegistry metricRegistry;

    public VBrokerRequestHandler(RequestHandlerFactory requestHandlerFactory,
                                 MetricRegistry metricRegistry) {
        this.requestHandlerFactory = requestHandlerFactory;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest vRequest) {
        log.info("== Received VRequest with correlationId {} and type {} ==", vRequest.correlationId(), vRequest.requestMessageType());
        RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(vRequest);

        String requestTypeName = RequestMessage.name(vRequest.requestMessageType());
        Timer timer = metricRegistry.timer(MetricRegistry.name(MetricUtils.brokerFullMetricName(requestTypeName)));
        Timer.Context context = timer.time();

        CompletionStage<VResponse> vResponseFuture = requestHandler.handle(vRequest);
        vResponseFuture.thenAccept(vResponse -> {
            long timeTakenNs = context.stop();
            double timeTakenMs = timeTakenNs / Math.pow(10, 6);
            log.info("Handled request processing of VRequestType {} with correlationId {} in {}ms",
                requestTypeName, vRequest.correlationId(), timeTakenMs);

            ByteBuf responseByteBuf = Unpooled.wrappedBuffer(vResponse.getByteBuffer());
            Response response = new Response(responseByteBuf.readableBytes(), responseByteBuf);

            ChannelFuture channelFuture = ctx.writeAndFlush(response);
            if (vResponse.responseMessageType() == ResponseMessage.ProduceResponse) {
                //for now, we want to close the channel for produce request when response is written
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception caught in server handling", cause);
        ctx.close();
    }
}
