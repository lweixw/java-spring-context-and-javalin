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


    // add index
    addNumberIndex(pipeline, "interestRate", 3.65, "1");
    addNumberIndex(pipeline, "interestRate", 3.45, "2");
    addNumberIndex(pipeline, "interestRate", 3.55, "3");
    addNumberIndex(pipeline, "interestRate", 3.75, "4");
    addNumberIndex(pipeline, "interestRate", 3.55, "5");

    addStringIndex(pipeline, "Funder", "ANZ", "3");
    addStringIndex(pipeline, "Funder", "CBA", "2");
    addStringIndex(pipeline, "Funder", "ANZ", "1");
    addStringIndex(pipeline, "Funder", "ANZ", "4");
    addStringIndex(pipeline, "Funder", "WESTPAC", "5");

    addFuzzyIndex(pipeline, "Funder", "ANZ", "3");
    addFuzzyIndex(pipeline, "Funder", "CBA", "2");
    addFuzzyIndex(pipeline, "Funder", "ANZ", "1");
    addFuzzyIndex(pipeline, "Funder", "ANZ", "4");
    addFuzzyIndex(pipeline, "Funder", "WESTPAC", "5");

    pipeline.sync();
    // queries
    var queryPipeline = jedis.pipelined();
    var rateIdxResponse = getNumberIndex(queryPipeline, "interestRate", 3.55, 3.65);
    var funderIdxResponse0 = startsWith(queryPipeline, "Funder", "C");
    var funderIdxResponse1 = endsWith(queryPipeline, "Funder", "Z");
    queryPipeline.sync();
    var anzProducts = getStringIndexByFullScan(jedis, "Funder", "ANZ");
    System.out.println(rateIdxResponse.get());
    System.out.println(funderIdxResponse0.get());
    System.out.println(funderIdxResponse1.get());
    System.out.println(anzProducts);

    var removePipeline = jedis.pipelined();
    removeNumberIndex(removePipeline,"interestRate", "5");
    removeFuzzyIndex(removePipeline, "Funder", "WESTPAC","5");
    removeStringIndex(removePipeline, "Funder", "ANZ", "1");
    removePipeline.sync();
  }
}
