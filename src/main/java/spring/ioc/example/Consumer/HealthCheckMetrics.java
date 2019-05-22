package spring.ioc.example.Consumer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCheckMetrics {
  final public int cpuCores;
  final public int activeThreads;
  final public String maxMemory;
  final public String totalMemory;
  final public String freeMemory;

  public HealthCheckMetrics(
      @JsonProperty("cpuCores") int cpuCores,
      @JsonProperty("activeThreads") int activeThreads,
      @JsonProperty("maxMemory") String maxMemory,
      @JsonProperty("totalMemory") String totalMemory,
      @JsonProperty("freeMemory") String freeMemory) {
    this.cpuCores = cpuCores;
    this.activeThreads = activeThreads;
    this.maxMemory = maxMemory;
    this.totalMemory = totalMemory;
    this.freeMemory = freeMemory;
  }
}
