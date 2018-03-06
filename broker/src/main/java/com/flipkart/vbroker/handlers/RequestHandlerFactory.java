package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.RequestMessage;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

@Slf4j
public class RequestHandlerFactory {

    private final RequestHandler produceRequestHandler;
    private final RequestHandler fetchRequestHandler;
    private final RequestHandler createTopicsRequestHandler;
    private final RequestHandler subscriptionCreateRequestHandler;
    private final GetSubscriptionLagsRequestHandler getSubscriptionLagsRequestHandler;
    private final GetSubscriptionsRequestHandler getSubscriptionsRequestHandler;
    private final GetTopicsRequestHandler getTopicsRequestHandler;

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
        this.createTopicsRequestHandler = new CreateTopicsRequestHandler(topicService, adminReqExecutorService);
        this.subscriptionCreateRequestHandler = new CreateSubscriptionsRequestHandler(subscriptionService, adminReqExecutorService);
        this.getSubscriptionLagsRequestHandler = new GetSubscriptionLagsRequestHandler(subscriptionService);
        this.getSubscriptionsRequestHandler = new GetSubscriptionsRequestHandler(subscriptionService);
        this.getTopicsRequestHandler = new GetTopicsRequestHandler(topicService);
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
            case RequestMessage.ControlRequest:
                requestHandler = getProtoRequestHandler(FlatbufUtils.getProtoRequest(request));
                break;
            default:
                throw new VBrokerException("Unknown RequestMessageType: " + request.requestMessageType());
        }
        return requestHandler;
    }

    private RequestHandler getProtoRequestHandler(ProtoRequest protoRequest) {
        RequestHandler requestHandler;
        switch (protoRequest.getProtoRequestCase()) {
            case CREATETOPICSREQUEST:
                log.info("Request is of type TopicCreateRequest");
                requestHandler = createTopicsRequestHandler;
                break;
            case CREATESUBSCRIPTIONSREQUEST:
                requestHandler = subscriptionCreateRequestHandler;
                break;
            case GETSUBSCRIPTIONLAGSREQUEST:
                requestHandler = getSubscriptionLagsRequestHandler;
                break;
            case CREATEQUEUESREQUEST:
                throw new NotImplementedException();
//                break;
            case GETQUEUESREQUEST:
                throw new NotImplementedException();
//                break;
            case GETSUBSCRIPTIONSREQUEST:
                requestHandler =  getSubscriptionsRequestHandler;
                break;
            case GETTOPICSREQUEST:
                requestHandler = getTopicsRequestHandler;
                break;
            default:
                throw new VBrokerException("Unknown ProtoRequestType: " + protoRequest.getProtoRequestCase());
        }
        return requestHandler;
    }
}
