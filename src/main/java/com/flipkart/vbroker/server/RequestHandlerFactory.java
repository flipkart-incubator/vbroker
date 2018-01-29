package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestHandlerFactory {

    private final RequestHandler produceRequestHandler;
    private final RequestHandler fetchRequestHandler;

    public RequestHandlerFactory(ProducerService producerService,
                                 TopicService topicService,
                                 SubscriptionService subscriptionService) {
        this.produceRequestHandler = new ProduceRequestHandler(topicService, producerService);
        this.fetchRequestHandler = new FetchRequestHandler(topicService, subscriptionService);
    }

    public RequestHandler getRequestHandler(VRequest request) {
        RequestHandler requestHandler;
        switch (request.requestMessageType()) {
            case RequestMessage.ProduceRequest:
                log.info("Request is of type ProduceRequest");
                requestHandler = produceRequestHandler;
                break;
            case RequestMessage.FetchRequest:
                log.info("Request is of type FetchRequest");
                requestHandler = fetchRequestHandler;
                break;
            default:
                throw new VBrokerException("Unknown RequestMessageType: " + request.requestMessageType());
        }
        return requestHandler;
    }
}
