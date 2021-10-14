package com.mycodefu.example;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

import static com.mycodefu.example.MongoAccess.*;

public class MainVerticle extends AbstractVerticle {
  private MongoClient mongo;
  private Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initMongo();
    initRoutes();

    final int listening_port = config().getInteger("http.port", 8888);
    vertx.createHttpServer().requestHandler(router).listen(listening_port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.printf("HTTP server started. Listening on http://localhost:%d\n", listening_port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private Router initRoutes() {
    this.router = Router.router(vertx);
    router.route().failureHandler(ErrorHandler.create(vertx, config().getBoolean("devmode", false)));
    router.route().handler(StaticHandler.create().setCachingEnabled(false));
    router.get("/examples.json").respond(ctx -> getExamples(mongo));
    router.post("/examples.json").handler(BodyHandler.create()).respond(ctx -> insertExample(mongo, Data.ExampleData.fromJson(ctx.getBodyAsJson())));
    router.delete("/examples.json").handler(BodyHandler.create()).respond(ctx -> deleteExample(mongo, ctx.queryParam("id").get(0)));
    return router;
  }

  private void initMongo() {
    this.mongo = MongoClient.createShared(vertx, config());
    createSampleData(mongo).onFailure(throwable -> {
      System.out.println("Failed creating some MongoDB data during initialization:");
      throwable.printStackTrace();
    }).onSuccess(ids -> {
      System.out.printf("Initialized MongoDB with test data (%s)!\n", ids);
    });
  }


  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(100));

    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "conf.json"));

    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");

    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(fileStore)
      .addStore(sysPropsStore);

    ConfigRetriever.create(vertx, options).getConfig()
      .onFailure(throwable -> {
        System.out.println("Failed to read the config!");
        throwable.printStackTrace();
        System.exit(2);
      })
      .onSuccess(entries -> {
        System.out.println("Successfully loaded config.");

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(entries);

        vertx.deployVerticle(MainVerticle.class, deploymentOptions)
          .onFailure(throwable -> {
            System.out.println("Failed deployment of MainVerticle!");
            throwable.printStackTrace();
            System.exit(1);
          })
          .onSuccess(id -> {
            System.out.printf("Successfully deployed MainVerticle (ID: %s)!\n\n", id);
          });
      });
  }
}
