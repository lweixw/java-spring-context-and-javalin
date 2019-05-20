package spring.ioc.example.SomeOtherStuff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.ioc.example.Consumer.BarService;

@Configuration
@Import(spring.ioc.example.Consumer.ConsumerConfig.class)
public class SomeOtherStuffConfig {

  @Bean
  @Autowired
  AnotherService anotherService(BarService barService) {
    return new AnotherService(barService);
  }
}
