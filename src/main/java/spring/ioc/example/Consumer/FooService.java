package spring.ioc.example.Consumer;

import org.springframework.stereotype.Component;

public class FooService {
	private BarService barService;
	public FooService(BarService barService) {
		this.barService = barService;
	}

	public void start() {
		System.out.println("FooService start");
		this.barService.handle();
	}
}
