package com.flipkart.vbroker.rest;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VBrokerRESTAppTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(VBrokerRESTApp.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void shouldServerBeStarted(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient().getNow(VBrokerRESTApp.restServerPort, "localhost", "/topics/1", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(body -> {
        tc.assertTrue(body.length() > 0);
        async.complete();
      });
    });
  }

}
