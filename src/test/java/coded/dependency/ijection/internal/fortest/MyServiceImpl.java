package coded.dependency.ijection.internal.fortest;

public class MyServiceImpl implements MyService {

	private boolean initialized;
	private boolean isStopped;

	@Override
	public void initialize() {
		initialized = true;
		System.out.println("MyService initialized.");
	}

	@Override
	public void destroy() {
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
