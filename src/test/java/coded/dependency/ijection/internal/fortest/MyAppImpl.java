package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;

public class MyAppImpl implements MyAppInterface {

	private Dependency<MyServiceInterface> svc = new Dependency<>(this, MyServiceInterface.class);
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
