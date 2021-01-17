package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Lifecycle;

public interface MyServiceInterface extends Lifecycle {

	public void start();

	public void stop();

	public String greets();
}
