package spring.ioc.example.SomeOtherStuff;

import spring.ioc.example.Consumer.BarService;

public class AnotherService {

  private BarService barService;

  public AnotherService(BarService barService) {
    this.barService = barService;
  }

  public void start() {
    System.out.println("AnotherService start");
		System.out.println("I can use BarServer");
		this.barService.handle();
  }
}
