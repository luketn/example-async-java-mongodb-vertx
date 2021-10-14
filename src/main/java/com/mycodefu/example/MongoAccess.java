package com.mycodefu.example;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoAccess {
  private final static String collectionName = "Examples";

  public static Future<List<Data.ExampleData>> getExamples(MongoClient mongo) {
    return mongo.find(collectionName, new JsonObject()).map(Data.ExampleData::fromJson);
  }

  public static Future<List<Data.ExampleData>> createSampleData(MongoClient mongo) {
    Promise<List<Data.ExampleData>> result = Promise.promise();

    mongo.count(collectionName, new JsonObject(), count -> {
      if (count.succeeded()) {
        if (count.result() == 0) {
          System.out.printf("Creating data as there was none in MongoDB! Attempting to create some MongoDB test data in an '%s' collection...\n", collectionName);
          List<Data.ExampleData> examples = Arrays.asList(
            new Data.ExampleData("Luke", 44),
            new Data.ExampleData("Bob", 99)
          );
          System.out.println(Json.encode(examples));

          List<Future> insertedFutures = new ArrayList<>();
          for (Data.ExampleData example : examples) {
            insertedFutures.add(mongo.insert(collectionName, JsonObject.mapFrom(example)).map(example::withId));
          }
          CompositeFuture.all(insertedFutures)
            .onFailure(result::fail)
            .onSuccess(compositeFuture -> {
              result.complete(compositeFuture.list());
            });
        } else {
          result.complete(new ArrayList<>());
        }
      } else {
        // report the error
        result.fail(count.cause());
      }
    });
    return result.future();
  }

}
