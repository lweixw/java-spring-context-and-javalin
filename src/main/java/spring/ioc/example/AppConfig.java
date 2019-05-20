package spring.ioc.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.ioc.example.Consumer.ConsumerConfig;
import spring.ioc.example.SomeOtherStuff.SomeOtherStuffConfig;

@Configuration
@Import({SomeOtherStuffConfig.class, ConsumerConfig.class})
public class AppConfig {}
