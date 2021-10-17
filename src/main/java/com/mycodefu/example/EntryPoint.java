package com.mycodefu.example;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class EntryPoint {
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
        System.out.println("Successfully loaded config!");

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
