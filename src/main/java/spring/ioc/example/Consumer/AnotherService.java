package spring.ioc.example.Consumer;

import org.springframework.stereotype.Component;

@Component
public class AnotherService {

	public void start() {
		System.out.println("AnotherService start");
	}
}
