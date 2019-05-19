package spring.ioc.example.Consumer;

public class BarService {
	public BarService() {}
	public void start() {
		System.out.println("BarService start");
	}
	public void handle() {
		System.out.println("BarService handling");
	}
}
