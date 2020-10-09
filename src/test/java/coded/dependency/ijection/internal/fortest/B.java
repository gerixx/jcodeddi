package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Lifecycle;

public class B implements Lifecycle {

	public boolean isStarted;

	public String hello() {
		return "hello";
	}

	@Override
	public void start() {
		isStarted = true;
	}

	@Override
	public void stop() {
		isStarted = false;
	}

}
