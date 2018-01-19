package com.flipkart.vbroker;

import com.flipkart.vbroker.server.VBrokerServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) throws IOException, InterruptedException {
        log.info("== Starting VBrokerApp ==");

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: {}", config);

        VBrokerServer server = new VBrokerServer(config);
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
