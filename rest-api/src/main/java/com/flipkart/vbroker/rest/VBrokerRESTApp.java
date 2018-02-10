package com.flipkart.vbroker.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class VBrokerRESTApp extends AbstractVerticle {
  public static final int restServerPort = 9500;

  @Override
  public void start() {


    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());
    router.get("/topics/:topicId").handler(this::handleGetTopic);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(restServerPort);
  }

  private void handleGetTopic(RoutingContext routingContext) {
    short topicId = Short.parseShort(routingContext.request().getParam("topicId"));
    HttpServerResponse response = routingContext.response();
    response.putHeader("Content-Type", "application/json")
      .end(new JsonObject().put("topicName", "test_topic_1").encodePrettily());
  }
}
