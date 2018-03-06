package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.RequestMessage;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.proto.GetAllQueuesResponse;
import com.flipkart.vbroker.proto.GetClusterMetadataResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.services.*;
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
    private final RequestHandler createSubscriptionsRequestHandler;
    private final GetSubscriptionLagsRequestHandler getSubscriptionLagsRequestHandler;
    private final GetSubscriptionsRequestHandler getSubscriptionsRequestHandler;
    private final GetTopicsRequestHandler getTopicsRequestHandler;
    private final GetQueuesRequestHandler getQueuesRequestHandler;
    private final GetAllTopicsRequestHandler getAllTopicsRequesetHandler;
    private final GetAllSubscriptionsForTopicsRequestHandler getAllSubscriptionsForTopicsRequestHandler;
    private final CreateQueuesRequestHandler createQueuesRequestHandler;
    private final GetAllQueuesRequestHandler getAllQueuesResponse;
    private final GetClusterMetadataRequestHandler getClusterMetadataRequestHandler;


    public RequestHandlerFactory(ProducerService producerService,
                                 TopicService topicService,
                                 SubscriptionService subscriptionService,
                                 QueueService queueService,
                                 ClusterMetadataService clusterMetadataService) {
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
        this.createSubscriptionsRequestHandler = new CreateSubscriptionsRequestHandler(subscriptionService, adminReqExecutorService);
        this.createQueuesRequestHandler = new CreateQueuesRequestHandler(queueService);
        this.getSubscriptionLagsRequestHandler = new GetSubscriptionLagsRequestHandler(subscriptionService);
        this.getSubscriptionsRequestHandler = new GetSubscriptionsRequestHandler(subscriptionService);
        this.getTopicsRequestHandler = new GetTopicsRequestHandler(topicService);
        this.getQueuesRequestHandler = new GetQueuesRequestHandler(queueService);
        this.getAllQueuesResponse = new GetAllQueuesRequestHandler(queueService);
        this.getAllTopicsRequesetHandler = new GetAllTopicsRequestHandler(topicService);
        this.getAllSubscriptionsForTopicsRequestHandler = new GetAllSubscriptionsForTopicsRequestHandler(subscriptionService);
        this.getClusterMetadataRequestHandler = new GetClusterMetadataRequestHandler(clusterMetadataService);
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
                requestHandler = createSubscriptionsRequestHandler;
                break;
            case GETSUBSCRIPTIONLAGSREQUEST:
                requestHandler = getSubscriptionLagsRequestHandler;
                break;
            case CREATEQUEUESREQUEST:
                requestHandler = createQueuesRequestHandler;
                  break;
            case GETQUEUESREQUEST:
                requestHandler = getQueuesRequestHandler;
                break;
            case GETALLQUEUESREQUEST:
               requestHandler = getAllQueuesResponse;
               break;
            case GETSUBSCRIPTIONSREQUEST:
                requestHandler = getSubscriptionsRequestHandler;
                break;
            case GETALLSUBSCRIPTIONSFORTOPICSREQUEST:
                requestHandler = getAllSubscriptionsForTopicsRequestHandler;
                break;
            case GETTOPICSREQUEST:
                requestHandler = getTopicsRequestHandler;
                break;
            case GETALLTOPICSREQUEST:
                requestHandler = getAllTopicsRequesetHandler;
                break;
            case GETCLUSTERMETADATAREQUEST:
                requestHandler = getClusterMetadataRequestHandler;
                break;
            default:
                throw new VBrokerException("Unknown ProtoRequestType: " + protoRequest.getProtoRequestCase());
        }
        return requestHandler;
    }
}
