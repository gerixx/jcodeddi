package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;

public class MyApplicationImpl implements MyApplicationInterface {

	private Dependency<MyServiceInterface> svc = new Dependency<>(this, MyServiceInterface.class);
	private boolean isStarted;
	private boolean isStopped;

	@Override
	public void start() {
		System.out.println("MyApp starts...");
		isStarted = true;
	}

	public String getGreets() {
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
