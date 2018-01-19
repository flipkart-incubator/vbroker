package com.flipkart.vbroker;


import com.flipkart.vbroker.controller.Controller;
import com.flipkart.vbroker.controller.CuratorService;
import com.flipkart.vbroker.server.VBrokerServer;

import lombok.extern.slf4j.Slf4j;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) throws Exception {
        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        log.info("Configs: ", config);
        CuratorService curatorService = new CuratorService();
        Controller controller = new Controller(curatorService);
        controller.watch();
        VBrokerServer server = new VBrokerServer(config, curatorService);
        server.start();

        log.info("Hello, World VBroker!");
    }
}
