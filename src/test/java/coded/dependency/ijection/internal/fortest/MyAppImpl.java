package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;

public class MyAppImpl implements MyApp {

	private Dependency<MyService> svc = new Dependency<>(this, MyService.class);
	private boolean isStarted;
	private boolean isStopped;

	@Override
	public String start() {
		System.out.println("MyApp starts...");
		isStarted = true;
		return svc.get()
			.greets();
	}

	@Override
	public void stop() {
		System.out.println("MyApp stopped.");
		isStopped = true;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isStopped() {
		return isStopped;
	}

}
