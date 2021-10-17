package com.mycodefu.example;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.reactiverse.junit5.web.WebClientOptionsInject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.reactiverse.junit5.web.TestRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
  public static final int TEST_PORT = 9988;
  private static MongodProcess MONGO;
  private static int MONGO_PORT = 9182;

  @WebClientOptionsInject
  public WebClientOptions opts = new WebClientOptions()
    .setDefaultPort(TEST_PORT)
    .setDefaultHost("localhost");

  DeploymentOptions options = new DeploymentOptions()
    .setConfig(new JsonObject()
      .put("http_port", TEST_PORT)
      .put("db_name", "Test")
      .put("connection_string",
        "mongodb://localhost:" + MONGO_PORT)
    );

  @BeforeAll
  public static void initialize() throws IOException {
    MongodStarter starter = MongodStarter.getDefaultInstance();
    ImmutableMongodConfig mongodConfig = ImmutableMongodConfig.builder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
      .build();
    MongodExecutable mongodExecutable =
      starter.prepare(mongodConfig);
    MONGO = mongodExecutable.start();
  }

  @AfterAll
  public static void shutdown() {  MONGO.stop(); }

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), options, testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  public void testExample(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/examples.json"))
      .expect(
        statusCode(200),
        responseHeader("content-type", "application/json")
      )
      .send(testContext).onSuccess(bufferHttpResponse -> {
        JsonArray result = bufferHttpResponse.bodyAsJsonArray();
        assertEquals(2, result.size());
        assertEquals("Luke", result.getJsonObject(0).getString("Name"));
        assertEquals(44, result.getJsonObject(0).getString("Age"));
      });
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
