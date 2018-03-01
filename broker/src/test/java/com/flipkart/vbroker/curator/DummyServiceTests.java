package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerClientInitializer;
import com.flipkart.vbroker.data.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.handlers.ResponseHandlerFactory;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.SubscriptionServiceImpl;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.services.TopicServiceImpl;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class DummyServiceTests {

    VBrokerConfig config;
    CuratorService curatorService;
    TopicService topicService;
    SubscriptionServiceImpl subscriptionService;
    TopicPartDataManager topicPartDataManager;

    @BeforeClass
    public void init() throws Exception {
        config = VBrokerConfig.newConfig("broker.properties");
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
            new ExponentialBackoffRetry(1000, 5));
        curatorClient.start();
        AsyncCuratorFramework asyncZkClient = AsyncCuratorFramework.wrap(curatorClient);

        curatorService = new CuratorService(asyncZkClient);

        topicService = new TopicServiceImpl(config, curatorService);
        topicPartDataManager = new InMemoryTopicPartDataManager();
        SubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);
        subscriptionService = new SubscriptionServiceImpl(config, curatorService, topicPartDataManager, subPartDataManager, topicService);

    }

    //@Test
    public void testTopicCreate() throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

        FlatBufferBuilder builder = new FlatBufferBuilder();

        int topicName = builder.createString("newTopic2");

        int topic = Topic.createTopic(builder, (short) 202, topicName, true, (short) 1, (short) 3, TopicCategory.TOPIC);
        int topicCreateRequest = TopicCreateRequest.createTopicCreateRequest(builder, topic);
        int vRequest = VRequest.createVRequest(builder, (byte) 1, 1002, RequestMessage.CreateTopicsRequest,
            topicCreateRequest);
        builder.finish(vRequest);

        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        Request request = new Request(byteBuf.readableBytes(), byteBuf);
        channel.writeAndFlush(request);
        channel.closeFuture().sync();
    }

    @Test
    public void testCreateSubscription() throws Exception {

        short topicId = 17369;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        Channel channel = bootstrap.connect(config.getBrokerHost(), config.getBrokerPort()).sync().channel();

        FlatBufferBuilder builder = new FlatBufferBuilder();
        int nameOffset = builder.createString("varadhi-sub");
        int httpUriOffset = builder.createString("http://localhost:8080");
        int httpMethodOffset = builder.createString("POST");

        int codeRangeOffset = CodeRange.createCodeRange(builder, (short) 200, (short) 299);
        int codeRangesVectorOffset = CallbackConfig.createCodeRangesVector(builder, new int[]{codeRangeOffset});
        int callbackConfigOffset = CallbackConfig.createCallbackConfig(builder, codeRangesVectorOffset);

        int filterOperatorOffset = builder.createString("OR");
        int filterKeyValuesListOffset = builder.createString("");
        int subscriptionOffset = Subscription.createSubscription(builder, (short) 101, topicId, nameOffset, true,
            (short) 2, (short) 1000, SubscriptionType.STATIC, SubscriptionMechanism.PUSH, httpUriOffset,
            httpMethodOffset, false, filterOperatorOffset, filterKeyValuesListOffset, callbackConfigOffset);
        int subCreateRequest = SubscriptionCreateRequest.createSubscriptionCreateRequest(builder, subscriptionOffset);
        int vRequest = VRequest.createVRequest(builder, (byte) 1, 1002, RequestMessage.CreateSubscriptionsRequest,
            subCreateRequest);
        builder.finish(vRequest);

        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        Request request = new Request(byteBuf.readableBytes(), byteBuf);
        channel.writeAndFlush(request);
        channel.closeFuture().sync();

    }

    // @Test
    public void testGetSubscriptions() throws Exception {
        subscriptionService.getSubscriptionsForTopic((short) 11238).handleAsync((data, exception) -> {
            System.out.println("Got data...");
            System.out.println(data);
            System.out.println(exception);
            return null;
        });

        // List<String> ids = new ArrayList<>();
        // ids.add("11805");
        // subscriptionService.getSubscriptionsForIds((short) 11238,
        // ids).handleAsync((data, exception) -> {
        // System.out.println(data);
        // System.out.println(exception);
        // return null;
        // });
        Thread.sleep(200000);
    }

    class ClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new VBrokerClientCodec());
            ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(null);
            pipeline.addLast(new VBrokerClientInitializer(responseHandlerFactory));

        }
    }

}
