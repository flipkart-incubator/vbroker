package com.flipkart.vbroker.app;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.client.ProducerRecord;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.server.VBrokerServer;
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
import java.util.HashMap;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.*;

@Slf4j
public class AbstractVBrokerBaseTest {

    public static final int MOCK_HTTP_SERVER_PORT = 17230;
    //http mock uris
    public static final String URI_200 = "/200";
    public static final String URI_204 = "/204";
    public static final String URI_400 = "/400";
    public static final String URI_404 = "/404";
    public static final String URI_429 = "/429";
    public static final String URI_500 = "/500";
    public static final String URI_504 = "/504";
    public static final String URI_MOCK_APP = "/url";
    public static final String URI_MOCK_APP2 = "/url2";
    public static final String MOCK_APP_200_URL = String.format("http://localhost:%d/200", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_204_URL = String.format("http://localhost:%d/204", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_400_URL = String.format("http://localhost:%d/400", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_404_URL = String.format("http://localhost:%d/404", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_429_URL = String.format("http://localhost:%d/429", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_DUAL_RESPONSE_URL = String.format("http://localhost:%d/dummy", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_500_URL = String.format("http://localhost:%d/500", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_504_URL = String.format("http://localhost:%d/504", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_SLEEP_200_URL = String.format("http://localhost:%d/sleep200", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_SLEEP_404_URL = String.format("http://localhost:%d/sleep404", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_URL = String.format("http://localhost:%d/url", MOCK_HTTP_SERVER_PORT);
    public static final String MOCK_APP_URL2 = String.format("http://localhost:%d/url2", MOCK_HTTP_SERVER_PORT);
    //http mock bodies
    public static final String MOCK_RESPONSE_BODY = "hehehaaaaaaa";
    private static final Object shutdownTestSuiteLock = new Object();
    //broker port
    public static int BROKER_PORT;
    private static VBrokerServer server;
    private static boolean cleanedUpDone = false;
    protected StubServer httpServer;

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
            AbstractVBrokerBaseTest.tearDown();
            log.info("Shutdown done safely");
        }));

        log.info("Starting VBroker test suite");

        String configFile = "test-broker.properties";
        VBrokerConfig config = VBrokerConfig.newConfig(configFile);

        BROKER_PORT = config.getBrokerPort();
        server = new VBrokerServer(config);
        server.startAsync().awaitRunning();
    }

    @BeforeMethod
    public void beforeTest() {
        server.awaitRunning();

        httpServer = new StubServer(MOCK_HTTP_SERVER_PORT).run();

        whenHttp(httpServer).match(post(URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_204)).then(status(HttpStatus.NO_CONTENT_204), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_429)).then(status(HttpStatus.getHttpStatus(429)), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(URI_504)).then(status(HttpStatus.GATEWAY_TIMEOUT_504), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(patch(URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));

        //Sleep for 5 seconds
        whenHttp(httpServer).match(post("/sleep200")).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY), sleep(6000));
        whenHttp(httpServer).match(post("/sleep404")).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY), sleep(1000));
        whenHttp(httpServer).match(post(URI_MOCK_APP)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(URI_MOCK_APP2)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
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

    public ProducerRecord newProducerRecord(Topic topic, String group, byte[] payload) {
        Message message = MessageStore.getRandomMsg(group);
        return ProducerRecord.builder()
            .groupId(message.groupId())
            .messageId(message.messageId())
            .crc((byte) 1)
            .version((byte) 1)
            .seqNo(1)
            .topicId(topic.id())
            .attributes(201)
            .httpUri(String.format("http://localhost:%s/messages", MOCK_HTTP_SERVER_PORT))
            .httpMethod(ProducerRecord.HttpMethod.POST)
            .callbackTopicId(topic.id())
            .callbackHttpUri("http://localhost:12000/messages")
            .callbackHttpMethod(ProducerRecord.HttpMethod.POST)
            .headers(new HashMap<>())
            .payload(payload)
            .build();
    }
}
