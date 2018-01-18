package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.FetchResponse;
import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerClientHandler extends SimpleChannelInboundHandler<VResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, VResponse msg) {
        log.info("Received VResponse from server {}", msg);
        switch (msg.responseMessageType()) {
            case ResponseMessage.ProduceResponse:
                ProduceResponse produceResponse = (ProduceResponse) msg.responseMessage(new ProduceResponse());
                assert produceResponse != null;
                short statusCode = produceResponse.statusCode();
                log.info("Received ProduceResponse with statusCode {}", statusCode);
                break;
            case ResponseMessage.FetchResponse:
                FetchResponse fetchResponse = (FetchResponse) msg.responseMessage(new FetchResponse());
                assert fetchResponse != null;
                log.info("Received FetchResponse with statusCode {}", fetchResponse.statusCode());
                break;
        }
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in client handling", cause);
        ctx.close();
    }
}
