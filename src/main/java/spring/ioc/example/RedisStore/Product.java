package spring.ioc.example.RedisStore;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
  public final String id;
  public final String name;

  public Product(@JsonProperty("id") String id, @JsonProperty("name") String name) {
    this.id = id;
    this.name = name;
  }
}
