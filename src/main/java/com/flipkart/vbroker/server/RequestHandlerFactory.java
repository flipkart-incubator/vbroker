package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchRequest;
import com.flipkart.vbroker.entities.ProduceRequest;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.ioengine.MessageService;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RequestHandlerFactory {

    private final ProducerService producerService;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final MessageService messageService;

    public RequestHandler getRequestHandler(VRequest request, ChannelHandlerContext ctx) {
        RequestHandler requestHandler;
        switch (request.requestMessageType()) {
            case RequestMessage.ProduceRequest:
                log.info("Request is of type ProduceRequest");
                ProduceRequest produceRequest = (ProduceRequest) request.requestMessage(new ProduceRequest());
                requestHandler = new ProduceRequestHandler(ctx, produceRequest, topicService, producerService);
                break;
            case RequestMessage.FetchRequest:
                log.info("Request is of type FetchRequest");
                FetchRequest fetchRequest = (FetchRequest) request.requestMessage(new FetchRequest());
                requestHandler = new FetchRequestHandler(ctx, fetchRequest, topicService, subscriptionService, messageService);
                break;
            default:
                throw new VBrokerException("Unknown RequestMessageType: " + request.requestMessageType());
        }
        return requestHandler;
    }
}
