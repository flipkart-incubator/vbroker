package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

@Slf4j
public class RequestHandlerFactory {

    private final RequestHandler produceRequestHandler;
    private final RequestHandler fetchRequestHandler;
    private final RequestHandler topicCreateRequestHandler;
    private final RequestHandler subscriptionCreateRequestHandler;

    public RequestHandlerFactory(ProducerService producerService,
                                 TopicService topicService,
                                 SubscriptionService subscriptionService) {
        ListeningExecutorService produceReqExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        ListeningExecutorService adminReqExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        ListeningExecutorService directExecutorService = MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService());

        this.produceRequestHandler = new ProduceRequestHandler(
            topicService,
            producerService,
            produceReqExecutorService);
        this.fetchRequestHandler = new FetchRequestHandler(
            topicService,
            subscriptionService,
            directExecutorService);
        this.topicCreateRequestHandler = new TopicCreateRequestHandler(topicService, adminReqExecutorService);
        this.subscriptionCreateRequestHandler = new SubscriptionCreateRequestHandler(subscriptionService, adminReqExecutorService);
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
            case RequestMessage.CreateTopicsRequest:
                log.info("Request is of type TopicCreateRequest");
                requestHandler = topicCreateRequestHandler;
                break;
            case RequestMessage.CreateSubscriptionsRequest:
                requestHandler = subscriptionCreateRequestHandler;
                break;
            default:
                throw new VBrokerException("Unknown RequestMessageType: " + request.requestMessageType());
        }
        return requestHandler;
    }
}
