package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyServiceImplementation implements MyServiceInterface, Dependent {

	Dependency<B> b = new Dependency<>(this, B.class);

	private boolean initialized;
	private boolean isStopped;

	@Override
	public void start() {
		initialized = true;
		System.out.println("MyService initialized.");
	}

	@Override
	public void stop() {
		System.out.println("MyService destroyed.");
		isStopped = true;
	}

	@Override
	public String greets() {
		return "greets from my service";
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isStopped() {
		return isStopped;
	}

}
