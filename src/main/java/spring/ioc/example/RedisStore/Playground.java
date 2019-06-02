package spring.ioc.example.RedisStore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import spring.ioc.example.Consumer.HealthCheckMetrics;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.text.MessageFormat;
import static spring.ioc.example.RedisStore.index.RedisIndex.*;

public class Playground {

  static HealthCheckMetrics getMemoryMetrics() {
    Runtime rt = Runtime.getRuntime();
    return new HealthCheckMetrics(
        rt.availableProcessors(),
        ManagementFactory.getThreadMXBean().getThreadCount(),
        MessageFormat.format("{0} KB", rt.maxMemory() / 1024),
        MessageFormat.format("{0} KB", rt.totalMemory() / 1024),
        MessageFormat.format("{0} KB", rt.freeMemory() / 1024));
  }

  public static void main(String[] args) throws JsonProcessingException {
    final byte[] trailingByte = {(byte) 0xff};
//    Jedis jedis = new Jedis("localhost");
    Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
//    Jedis jedis = new Jedis(URI.create("rediss://master.de-product-svc.1nlvj0.apse2.cache.amazonaws.com:6379"));
    ObjectMapper objectMapper = new ObjectMapper();
    String data = objectMapper.writeValueAsString(getMemoryMetrics());
    jedis.hset("Product", "21902750-7c74-11e9-8f9e-2a86e4085a59", data);
    System.out.println(jedis.hget("Product", "21902750-7c74-11e9-8f9e-2a86e4085a59"));

    var pipeline = jedis.pipelined();
    pipeline.hset("Product", "1", "value1");
    pipeline.hset("Product", "2", "value2");
    pipeline.hset("Product", "3", "value3");
    pipeline.hset("Product", "4", "value4");
    pipeline.hset("Product", "5", "value5");


//    pipeline.zadd("_idxRate", 3.65, "1");
//    pipeline.zadd("_idxRate", 3.45, "2");
//    pipeline.zadd("_idxRate", 3.55, "3");
//    pipeline.zadd("_idxRate", 3.75, "4");
//    pipeline.zadd("_idxRate", 3.55, "5");
//
//    pipeline.zadd("_idxFunder:ANZ", 0, "3");
//    pipeline.zadd("_idxFunder:CBA", 0, "2");
//    pipeline.zadd("_idxFunder:ANZ", 0, "1");
//    pipeline.zadd("_idxFunder:ANZ", 0, "4");
//    pipeline.zadd("_idxFunder:WESTPAC", 0, "5");

    // add index
    addIndex(pipeline, "interestRate", 3.65, "1");
    addIndex(pipeline, "interestRate", 3.45, "2");
    addIndex(pipeline, "interestRate", 3.55, "3");
    addIndex(pipeline, "interestRate", 3.75, "4");
    addIndex(pipeline, "interestRate", 3.55, "5");

    addIndex(pipeline, "Funder", "ANZ", "3");
    addIndex(pipeline, "Funder", "CBA", "2");
    addIndex(pipeline, "Funder", "ANZ", "1");
    addIndex(pipeline, "Funder", "ANZ", "4");
    addIndex(pipeline, "Funder", "WESTPAC", "5");

    addFuzzyIndex(pipeline, "Funder", "ANZ", "3");
    addFuzzyIndex(pipeline, "Funder", "CBA", "2");
    addFuzzyIndex(pipeline, "Funder", "ANZ", "1");
    addFuzzyIndex(pipeline, "Funder", "ANZ", "4");
    addFuzzyIndex(pipeline, "Funder", "WESTPAC", "5");

    pipeline.sync();
    // queries
    var queryPipeline = jedis.pipelined();
    var rateIdxResponse = getIndex(queryPipeline, "interestRate", 3.55, 3.65);
    var funderIdxResponse = endsWith(queryPipeline, "Funder", "Z");
    queryPipeline.sync();
    System.out.println(rateIdxResponse.get());
    System.out.println(funderIdxResponse.get());
  }
}
