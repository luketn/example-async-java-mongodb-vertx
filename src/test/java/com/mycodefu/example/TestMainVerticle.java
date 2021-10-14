package com.mycodefu.example;

import io.reactiverse.junit5.web.WebClientOptionsInject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.reactiverse.junit5.web.TestRequest.*;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @WebClientOptionsInject
  public WebClientOptions opts = new WebClientOptions()
    .setDefaultPort(8888)
    .setDefaultHost("localhost");

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  public void testExample(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/example.json"))
      .expect(
        statusCode(200),
        responseHeader("content-type", "application/json"),
        jsonBodyResponse(new JsonObject().put("name", "Luke").put("age", 44))
      )
      .send(testContext);
  }

  @Test
  public void testRoot(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/"))
      .expect(
        statusCode(200),
        responseHeader("content-type", "text/html;charset=UTF-8")
      )
      .send(testContext);
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
}
