package com.mycodefu.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Data {
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonSerialize
  public static record ExampleData(String id, String name, int age) {
    public ExampleData(String name, int age) {
      this(null, name, age);
    }

    public ExampleData withId(String id) {
      return new ExampleData(id, name, age);
    }

    public static ExampleData fromJson(JsonObject jsonObject) {
      return new ExampleData(
        jsonObject.getString("_id"),
        jsonObject.getString("name"),
        jsonObject.getInteger("age")
      );
    }

    public static List<ExampleData> fromJson(List<JsonObject> jsonObjects) {
      List<ExampleData> result = new ArrayList<>();
      for (JsonObject jsonObject : jsonObjects) {
        result.add(ExampleData.fromJson(jsonObject));
      }
      return result;
    }
  }
}
