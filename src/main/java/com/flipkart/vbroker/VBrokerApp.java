package com.flipkart.vbroker;


import com.flipkart.vbroker.controller.VBrokerController;
import com.flipkart.vbroker.server.VBrokerServer;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;

import lombok.extern.slf4j.Slf4j;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) throws Exception {
        log.info("== Starting VBrokerApp ==");

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: {}", config);

        CuratorService curatorService = new CuratorService();
        VBrokerController controller = new VBrokerController(curatorService);
        controller.watch();
        TopicService topicService = new TopicService(curatorService);
        VBrokerServer server = new VBrokerServer(config, curatorService, topicService);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (InterruptedException e) {
                log.error("Exception in closing channels", e);
            }
            log.info("== Shutting down VBrokerServer ==");
        }));

        log.info("Starting VBrokerServer");
        Thread thread = new Thread(server);
        thread.start();

        thread.join();
        log.info("== Shutting down VBrokerApp ==");
    }
}
