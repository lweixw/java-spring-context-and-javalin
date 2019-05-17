package spring.ioc.example.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "spring.ioc.example")
public class Config {
	@Bean
	@Autowired
	FooService fooService(BarService barService) {
		return new FooService(barService);
	}

	@Bean
	BarService barService() {
		return new BarService();
	}

}
