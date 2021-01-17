package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependent;
import coded.dependency.injection.Lifecycle;

public interface MyApplicationInterface extends Dependent, Lifecycle {

	public void start();

	public void stop();

}
