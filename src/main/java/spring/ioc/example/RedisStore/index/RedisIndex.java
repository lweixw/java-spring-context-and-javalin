package spring.ioc.example.RedisStore.index;

import com.google.common.primitives.Bytes;
import redis.clients.jedis.*;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public final class RedisIndex {
  private static String rangeIndexKey(String attributeName) {
    return "_idx_" + attributeName;
  }

  private static String rangeIndexKeyReversed(String attributeName) {
    return "_idx_" + attributeName + "_reversed";
  }

  private static String indexKeyForString(String attributeName, String value) {
    return "_idx_" + attributeName + ":" + value;
  }

  private static String rangeMinForLex(String value) {
    return "[" + value;
  }

  private static String rangeMaxForLex(String value) {
    final byte[] trailing = new byte[] {(byte) 0xff};
    String min = "[" + value;
    return new String(Bytes.concat(min.getBytes(), trailing));
  }

  private static String reverseStringValue(String value) {
    return new StringBuilder(value).reverse().toString();
  }

  public static Pipeline addIndex(
      Pipeline redisPipeline, String attributeName, double value, String targetId) {
    redisPipeline.zadd(rangeIndexKey(attributeName), value, targetId);
    return redisPipeline;
  }

  public static Pipeline addIndex(
      Pipeline redisPipeline, String attributeName, String value, String targetId) {
    redisPipeline.sadd(indexKeyForString(attributeName, value), targetId);
    return redisPipeline;
  }

  public static Pipeline addFuzzyIndex(
    Pipeline redisPipeline, String attributeName, String value, String targetId) {
    redisPipeline.zadd(
      rangeIndexKey(attributeName), 0, MessageFormat.format("{0}:{1}", value, targetId));
    redisPipeline.zadd(
      rangeIndexKeyReversed(attributeName),
      0,
      MessageFormat.format("{0}:{1}", reverseStringValue(value), targetId));
    return redisPipeline;
  }

  public static Response<Set<String>> getIndex(
      Pipeline redisPipeline, String attributeName, double min, double max) {
    return redisPipeline.zrangeByScore(rangeIndexKey(attributeName), min, max);
  }

  public static Response<Set<String>> getIndex(
      Pipeline redisPipeline, String attributeName, double min, double max, int offset, int limit) {
    return redisPipeline.zrangeByScore(rangeIndexKey(attributeName), min, max, offset, limit);
  }

  public static Response<Set<String>> getIndex(
      Pipeline redisPipeline, String attributeName, String value) {
    return redisPipeline.smembers(indexKeyForString(attributeName, value));
  }

  public static Set<String> getIndex(Jedis redis, String attributeName, String value) {
    Set<String> result = new HashSet<>();
    String cursor = "0";
    while (true) {
      var scanResult = redis.sscan(rangeIndexKey(attributeName) + ":" + value, cursor);
      result.addAll(scanResult.getResult());
      if (scanResult.isCompleteIteration()) {
        break;
      }
      cursor = scanResult.getCursor();
    }
    return result;
  }

  public static ScanResult<String> scanIndex(
      Jedis redis, String attributeName, String value, String cursor, int count) {
    return redis.sscan(
        rangeIndexKey(attributeName) + ":" + value, cursor, new ScanParams().count(count));
  }

  public static Response<Set<String>> startsWith(
      Pipeline redisPipeline, String attributeName, String startsWith) {
    return redisPipeline.zrangeByLex(
        rangeIndexKey(attributeName), rangeMinForLex(startsWith), rangeMaxForLex(startsWith));
  }

  public static Response<Set<String>> startsWith(
      Pipeline redisPipeline, String attributeName, String startsWith, int offset, int limit) {
    return redisPipeline.zrangeByLex(
        rangeIndexKey(attributeName),
        rangeMinForLex(startsWith),
        rangeMaxForLex(startsWith),
        offset,
        limit);
  }

  public static Response<Set<String>> endsWith(
      Pipeline redisPipeline, String attributeName, String endsWidth) {
    String reversed = reverseStringValue(endsWidth);
    return redisPipeline.zrangeByLex(
        rangeIndexKeyReversed(attributeName), rangeMinForLex(reversed), rangeMaxForLex(reversed));
  }

  public static Response<Set<String>> endsWith(
      Pipeline redisPipeline, String attributeName, String endsWidth, int offset, int limit) {
    String reversed = reverseStringValue(endsWidth);
    return redisPipeline.zrangeByLex(
        rangeIndexKeyReversed(attributeName),
        rangeMinForLex(reversed),
        rangeMaxForLex(reversed),
        offset,
        limit);
  }
}
