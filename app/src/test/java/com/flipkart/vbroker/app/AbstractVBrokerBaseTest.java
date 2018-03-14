package com.flipkart.vbroker.app;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.*;
import com.flipkart.vbroker.proto.CreateTopicResponse;
import com.flipkart.vbroker.proto.CreateTopicsRequest;
import com.flipkart.vbroker.proto.ProtoTopic;
import com.flipkart.vbroker.proto.TopicCategory;
import com.flipkart.vbroker.server.VBrokerServer;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.server.StubServer;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.flipkart.vbroker.app.MockHttp.*;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;

@Slf4j
public class AbstractVBrokerBaseTest {
    private static final Object shutdownTestSuiteLock = new Object();

    //CONSTANTS
    //broker port
    public static int BROKER_PORT;

    private static VBrokerServer server;
    private static boolean cleanedUpDone = false;
    protected StubServer httpServer;
    private VBrokerConfig config;

    @AfterClass
    public static void tearDown() {
        synchronized (shutdownTestSuiteLock) {
            if (!cleanedUpDone) {
                log.info("Shutting down VBroker test suite");
                server.stopAsync().awaitTerminated();
                cleanedUpDone = true;
                log.info("Test suite cleanup done");
            }
        }
    }

    @BeforeClass
    public void setUp() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Handled Shutdown SIGNAL. Shutting down the test suite safely");
            tearDown();
            log.info("Shutdown done safely");
        }));

        log.info("Starting VBroker test suite");

        String configFile = "test-broker.properties";
        config = VBrokerConfig.newConfig(configFile);

        BROKER_PORT = config.getBrokerPort();
        server = new VBrokerServer(config);
        server.startAsync().awaitRunning();
    }

    @BeforeMethod
    public void beforeTest() {
        server.awaitRunning();

        httpServer = new StubServer(MOCK_HTTP_SERVER_PORT).run();
        httpServer.setRegisterCalls(true);

        IntStream.range(250, 300)
            .forEachOrdered(code -> {
                whenHttp(httpServer).match(com.xebialabs.restito.semantics.ConditionWithApplicables.post("/" + code))
                    .then(status(HttpStatus.newHttpStatus(code, "test_success_code_" + code)),
                        stringContent(MOCK_RESPONSE_BODY));
            });

        whenHttp(httpServer).match(post(MockURI.URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_201)).then(status(HttpStatus.CREATED_201), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_202)).then(status(HttpStatus.ACCEPTED_202), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_204)).then(status(HttpStatus.NO_CONTENT_204), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_429)).then(status(HttpStatus.getHttpStatus(429)), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_504)).then(status(HttpStatus.GATEWAY_TIMEOUT_504), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(patch(MockURI.URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_MOCK_APP)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_MOCK_APP2)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        //Sleep for 5 seconds
        whenHttp(httpServer).match(post(MockURI.SLEEP_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY), sleep(1000));
        whenHttp(httpServer).match(post(MockURI.SLEEP_201)).then(status(HttpStatus.CREATED_201), stringContent(MOCK_RESPONSE_BODY), sleep(2000));
        whenHttp(httpServer).match(post(MockURI.SLEEP_202)).then(status(HttpStatus.ACCEPTED_202), stringContent(MOCK_RESPONSE_BODY), sleep(3000));
        whenHttp(httpServer).match(post(MockURI.SLEEP_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY), sleep(1000));
    }

    protected void clearHttpCalls() {
        if (httpServer != null) {
            //httpServer.clear();
            httpServer.clearCalls();
        }
    }

    @AfterMethod
    public void afterTest() {
        //cleanup http calls to the stub server
        clearHttpCalls();
        httpServer.stop();
    }

    private Action sleep(long sleepTime) {
        return Action.delay((int) sleepTime);
    }

    public Topic createRandomTopic(boolean grouped) {
        ProtoTopic protoTopic = ProtoTopic.newBuilder()
            .setId(new Random().nextInt())
            .setName(UUID.randomUUID().toString())
            .setGrouped(grouped)
            .setTopicCategory(TopicCategory.TOPIC)
            .setPartitions(3)
            .setReplicationFactor(1)
            .build();
        Topic topic = new Topic(protoTopic);

        try {
            TopicClient topicClient = new TopicClient(new NetworkClientImpl(), new MetadataImpl(VBClientConfig.newConfig("client.properties")));
            List<CreateTopicResponse> createTopicResponses = topicClient.createTopics(Collections.singletonList(topic)).toCompletableFuture().join();
            return topic;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Topic createUnGroupedTopic() {
        return DummyEntities.unGroupedTopic;
    }
}
