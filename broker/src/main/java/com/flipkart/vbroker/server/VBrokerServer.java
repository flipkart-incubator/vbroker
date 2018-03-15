package com.flipkart.vbroker.server;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.controller.VBrokerController;
import com.flipkart.vbroker.data.DataManagerFactory;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.handlers.RequestHandlerFactory;
import com.flipkart.vbroker.services.*;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.util.List;
import java.util.concurrent.*;

import static java.util.Objects.nonNull;

@Slf4j
public class VBrokerServer extends AbstractExecutionThreadService {

    private final VBrokerConfig config;

    private final CountDownLatch mainLatch = new CountDownLatch(1);
    private Channel serverChannel;
    private Channel serverLocalChannel;
    private VBrokerController brokerController;

    private Subscriber subscriber;
    private ExecutorService subscriberExecutor;
    private ExecutorService coordinatorExecutor;//used for curator, in-memory topic/subscription services

    private CuratorFramework curatorClient;

    public VBrokerServer(VBrokerConfig config) {
        this.config = config;
    }

    private void startServer() {
        Thread.currentThread().setName("vbroker_server");

        curatorClient = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
            new ExponentialBackoffRetry(1000, 5));
        curatorClient.start();
        AsyncCuratorFramework asyncZkClient = AsyncCuratorFramework.wrap(curatorClient);
        CuratorService curatorService = new CuratorService(asyncZkClient);

        MetricRegistry metricRegistry = new MetricRegistry();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("server_boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("server_worker"));
        EventLoopGroup localGroup = new DefaultEventLoopGroup(1, new DefaultThreadFactory("server_local"));

        //TopicService topicService = new TopicServiceImpl(config, curatorService);
        ThreadFactory coordinatorThreadFactory = new ThreadFactoryBuilder().setNameFormat("coordinator_pool-%d").build();
        coordinatorExecutor = Executors.newCachedThreadPool(coordinatorThreadFactory);
        TopicService topicService = new InMemoryTopicService(coordinatorExecutor);

        DataManagerFactory dataManagerFactory = new DataManagerFactory(config, workerGroup);
        TopicPartDataManager topicPartDataManager = dataManagerFactory.getTopicDataManager();
        SubPartDataManager subPartDataManager = dataManagerFactory.getSubPartDataManager(topicPartDataManager);

        SubscriptionService subscriptionService = new InMemorySubscriptionService(
            topicService,
            topicPartDataManager,
            subPartDataManager,
            coordinatorExecutor);

        QueueService queueService = null;
        ClusterMetadataService clusterMetadataService = null;

        //global broker controller
        brokerController = new VBrokerController(curatorService, topicService, subscriptionService, config);
        log.info("Starting controller and awaiting it to be ready");
        brokerController.startAsync().awaitRunning();

        //TODO: now that metadata is created, we need to add actual data to the metadata entries
        //=> populate message groups in topic partitions

//        List<Topic> topics = Lists.newArrayList(DummyEntities.groupedTopic, DummyEntities.unGroupedTopic);
//        CompletableFuture[] topicFutures = topics
//            .stream()
//            .map(topic -> topicService.createTopic(topic).toCompletableFuture())
//            .toArray(CompletableFuture[]::new);
//        CompletableFuture.allOf(topicFutures).join();
//        log.info("Created dummy topics");
//
//        List<Subscription> subscriptions = Lists.newArrayList(
//            DummyEntities.groupedSubscription,
//            DummyEntities.unGroupedSubscription);
//        CompletableFuture[] subscriptionFutures = subscriptions
//            .stream()
//            .map(subscription -> subscriptionService.createSubscription(subscription).toCompletableFuture())
//            .toArray(CompletableFuture[]::new);
//        CompletableFuture.allOf(subscriptionFutures).join();
//        log.info("Created dummy subscriptions");

        ProducerService producerService = new ProducerService(topicPartDataManager);
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(
            producerService, topicService, subscriptionService, queueService, clusterMetadataService);

        DefaultAsyncHttpClientConfig httpClientConfig = new DefaultAsyncHttpClientConfig
            .Builder()
            //.setEventLoopGroup(workerGroup) //using same worker group event loop
            //.setThreadFactory(new DefaultThreadFactory("async_http_client"))
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async_http_client-%d").build())
            .build();
        AsyncHttpClient httpClient = new DefaultAsyncHttpClient(httpClientConfig);
        MessageProcessor messageProcessor = new HttpMessageProcessor(httpClient,
            topicService,
            subscriptionService,
            producerService,
            subPartDataManager,
            coordinatorExecutor,
            metricRegistry);

        subscriber = new Subscriber(subscriptionService,
            messageProcessor,
            config,
            metricRegistry);

        CountDownLatch latch = new CountDownLatch(1);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new VBrokerServerInitializer(requestHandlerFactory));

            ExecutorService remoteServerExecutor = Executors.newSingleThreadExecutor();
            remoteServerExecutor.submit(() -> {
                try {
                    serverChannel = serverBootstrap.bind(config.getBrokerHost(), config.getBrokerPort()).sync().channel();
                    log.info("Broker now listening on port {}", config.getBrokerPort());

                    //the broker can now startServer accepting new requests
                    //TODO: declare broker as healthy by registering in /brokers/ids for example now that everything is validated

                    log.info("Starting subscriber");
                    ThreadFactory subscriberThreadFactory = new ThreadFactoryBuilder().setNameFormat("subscriber_pool-%d").build();
                    subscriberExecutor = Executors.newCachedThreadPool(subscriberThreadFactory);
                    subscriberExecutor.submit(subscriber);

                    serverChannel.closeFuture().sync();
                    latch.countDown();
                } catch (InterruptedException e) {
                    log.error("Exception in channel sync", e);
                }
            });


//            LocalAddress address = new LocalAddress(new Random().nextInt(60000) + "");
//            setupLocalSubscribers(localGroup, workerGroup, address, subscriptionService);
//            //below used for local channel by the consumer
//            ServerBootstrap serverLocalBootstrap = new ServerBootstrap();
//            serverLocalBootstrap.group(localGroup, localGroup)
//                    .channel(LocalServerChannel.class)
//                    .handler(new LoggingHandler())
//                    .childHandler(new VBrokerServerInitializer(requestHandlerFactory));
//
//            ExecutorService localServerExecutor = Executors.newSingleThreadExecutor();
//            localServerExecutor.submit(() -> {
//                try {
//                    serverLocalChannel = serverLocalBootstrap.bind(address).sync().channel();
//                    log.info("Consumer now listening on address {}", address);
//
//                    serverLocalChannel.closeFuture().sync();
//                    log.info("Consumer serverLocalChannel closed");
//                    latch.countDown();
//                } catch (InterruptedException e) {
//                    log.error("Exception in channel sync", e);
//                }
//            });
            log.debug("Awaiting on latch");
            latch.await();

            mainLatch.countDown();
            log.debug("Latch countdown complete");
        } catch (InterruptedException e) {
            log.error("Exception in binding to/closing a channel", e);
        } finally {
            localGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void stopServer() throws InterruptedException {
        log.info("Stopping the server");
        if (serverChannel != null) {
            log.info("Closing serverChannel");
            serverChannel.close();
        }

        if (serverLocalChannel != null) {
            log.info("Closing serverLocalChannel");
            serverLocalChannel.close();
        }

        log.info("Waiting for servers to shutdown peacefully");
        mainLatch.await();

        if (nonNull(brokerController)) {
            log.info("Stopping VBrokerController");
            brokerController.stopAsync().awaitTerminated();
        }

        if (nonNull(subscriber)) {
            log.info("Stopping Subscriber");
            subscriber.stop();
        }

        log.info("Stopping Subscriber ExecutorService");
        subscriberExecutor.shutdown();
        MoreExecutors.shutdownAndAwaitTermination(
            subscriberExecutor, 2, TimeUnit.SECONDS);

        if (nonNull(curatorClient)) {
            log.info("Closing zookeeper client");
            curatorClient.close();
        }

        log.info("Stopping Coordinator ExecutorService");
        coordinatorExecutor.shutdown();
        MoreExecutors.shutdownAndAwaitTermination(
            coordinatorExecutor, 1, TimeUnit.SECONDS);

        log.info("Yay! Clean stop of VBrokerServer is successful");
    }

//    private void setupLocalSubscribers(EventLoopGroup localGroup,
//                                       EventLoopGroup workerGroup,
//                                       LocalAddress address,
//                                       SubscriptionService subscriptionService) {
//        Bootstrap httpClientBootstrap = new Bootstrap()
//            .group(workerGroup)
//            .channel(NioSocketChannel.class)
//            .handler(new ChannelInitializer<Channel>() {
//                @Override
//                protected void initChannel(Channel ch) {
//                    ChannelPipeline pipeline = ch.pipeline();
//                    pipeline.addLast(new HttpClientCodec());
//                    pipeline.addLast(new HttpObjectAggregator(1024 * 1024)); //1MB max
//                    pipeline.addLast(new HttpResponseHandler());
//                }
//            });
//        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(httpClientBootstrap);
//        Bootstrap consumerBootstrap = new Bootstrap()
//            .group(localGroup)
//            .channel(LocalChannel.class)
//            .handler(new ChannelInitializer<Channel>() {
//                @Override
//                protected void initChannel(Channel ch) {
//                    ChannelPipeline pipeline = ch.pipeline();
//                    pipeline.addLast(new ChannelInitializer<Channel>() {
//                        @Override
//                        protected void initChannel(Channel ch) {
//                            pipeline.addLast(new VBrokerClientCodec());
//                            pipeline.addLast(new VBrokerResponseHandler(responseHandlerFactory));
//                        }
//                    });
//                }
//            });
//        SubscriberDaemon subscriber = new SubscriberDaemon(address, consumerBootstrap, subscriptionService);
//        ExecutorService subscriberExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("subscriber"));
//        subscriberExecutor.submit(subscriber);
//    }

    @Override
    protected void run() {
        startServer();
    }

    @Override
    protected void triggerShutdown() {
        log.info("service shutDown triggered");
        try {
            stopServer();
        } catch (Exception ex) {
            log.error("Exception in stopping the VBrokerServer. Ignoring", ex);
        }
    }
}
