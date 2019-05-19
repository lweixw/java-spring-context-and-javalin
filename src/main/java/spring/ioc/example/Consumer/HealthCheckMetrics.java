package spring.ioc.example.Consumer;

import lombok.Value;

@Value
public class HealthCheckMetrics {
	private int cpuCores;
	private int activeThreads;
	private String maxMemory;
	private String totalMemory;
	private String freeMemory;
}
