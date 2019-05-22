package spring.ioc.example.RedisStore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.primitives.Bytes;
import redis.clients.jedis.Jedis;
import spring.ioc.example.Consumer.HealthCheckMetrics;

import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;

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
    Jedis jedis = new Jedis("localhost");
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

    pipeline.zadd("_idxRate", 3.65, "1");
    pipeline.zadd("_idxRate", 3.45, "2");
    pipeline.zadd("_idxRate", 3.55, "3");
    pipeline.zadd("_idxRate", 3.75, "4");
    pipeline.zadd("_idxRate", 3.55, "5");

    pipeline.zadd("_idxFunder", 0, "ANZ:1");
    pipeline.zadd("_idxFunder", 0, "CBA:2");
    pipeline.zadd("_idxFunder", 0, "ANZ:3");
    pipeline.zadd("_idxFunder", 0, "ING:4");
    pipeline.zadd("_idxFunder", 0, "WESTPAC:5");

    String funderQuery = "[ANZ";
    var rateResult = pipeline.zrangeByScore("_idxRate", 3.40, 3.60);
    var funderResult =
        pipeline.zrangeByLex(
            "_idxFunder".getBytes(),
            funderQuery.getBytes(),
            Bytes.concat(funderQuery.getBytes(), trailingByte));

    pipeline.sync();
    System.out.println(rateResult.get());
    Set<String> rates = rateResult.get();
    Set<String> funders =
        funderResult.get().stream()
            .map(
                bytes -> {
                  String value = new String(bytes);
                  //      value.substring(value.lastIndexOf(":"));
                  return value.substring(value.lastIndexOf(":") + 1);
                })
            .collect(Collectors.toSet());
    Set<String> result = Sets.intersection(funders, rates);
    System.out.println(result);
    jedis.hmget("Product", result.toArray(n -> new String[n])).stream().forEach(System.out::println);
  }
}
