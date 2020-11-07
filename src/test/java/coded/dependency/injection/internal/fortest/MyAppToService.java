package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;

public class MyAppToService implements MyApplicationInterface {

	public Dependency<MyServiceInterface> service = new Dependency<>(this, MyServiceInterface.class);

	@Override
	public String start() {
		return null;
	}

	@Override
	public void stop() {
	}

}
