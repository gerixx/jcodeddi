package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Lifecycle;

public class Interface1And2Impl implements Interface1, Interface2, Lifecycle {

	private boolean isRunning;

	@Override
	public String getUsageInfoOfInterface1() {
		return "implementation of Interface1";
	}

	@Override
	public String getUsageInfoOfInterface2() {
		return "implementation of Interface2";
	}

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("Interface1And2Impl is already running");
		}
		isRunning = true;
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("Interface1And2Impl is not running");
		}
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

}
