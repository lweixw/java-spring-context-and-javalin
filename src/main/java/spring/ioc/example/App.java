package spring.ioc.example;

import io.javalin.Javalin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.ioc.example.Consumer.*;
import spring.ioc.example.SomeOtherStuff.AnotherService;

import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Optional;

public class App {

  static HealthCheckMetrics getMemoryMetrics() {
    Runtime rt = Runtime.getRuntime();
    return new HealthCheckMetrics(
        rt.availableProcessors(),
        ManagementFactory.getThreadMXBean().getThreadCount(),
        MessageFormat.format("{0} KB", rt.maxMemory() / 1024),
        MessageFormat.format("{0} KB", rt.totalMemory() / 1024),
        MessageFormat.format("{0} KB", rt.freeMemory() / 1024));
  }

  static void startWebServer() {
    int port = 80;
    try {
      port = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("80"));
    } catch (NumberFormatException nfe) {
      System.out.println("use default port " + port);
    }

    Javalin.create()
        .defaultContentType("application/json")
        .requestLogger(
            (ctx, timeMs) ->
                System.out.println(ctx.method() + " " + ctx.path() + " took " + timeMs + " ms"))
        .get("/health-check", ctx -> ctx.json(getMemoryMetrics()).header("Connection", "close"))
        .start(port);
  }

  static void startWorkers() {
    AnnotationConfigApplicationContext applicationContext =
        new AnnotationConfigApplicationContext();
    applicationContext.register(AppConfig.class);
    applicationContext.refresh();

    FooService fooService = applicationContext.getBean(FooService.class);
    BarService barService = applicationContext.getBean(BarService.class);
    AnotherService anotherService = applicationContext.getBean(AnotherService.class);
    barService.start();
    fooService.start();
    anotherService.start();
  }

  public static void main(String[] args) {
    startWorkers();
    startWebServer();

    System.out.println(getMemoryMetrics());
  }
}
