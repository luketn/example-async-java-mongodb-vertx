package com.mycodefu.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonSerialize
  private static record ExampleData(String name, int age) {
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    router.route().failureHandler(ErrorHandler.create(vertx));
    router.route().handler(StaticHandler.create().setCachingEnabled(false));

    router.get("/example.json").respond(ctx -> Future.succeededFuture(new ExampleData("Luke", 44)));

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started. Listening on http://localhost:8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(100));
    vertx.deployVerticle(new MainVerticle())
      .onFailure(throwable -> {
        System.out.println("Failed deployment of MainVerticle!");
        throwable.printStackTrace();
        System.exit(1);
      })
      .onSuccess(id -> {
        System.out.printf("Successfully deployed MainVerticle!\nID: %s\n\n", id);
      });
  }
}
