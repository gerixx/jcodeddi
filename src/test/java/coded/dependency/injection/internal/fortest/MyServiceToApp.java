package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyServiceToApp implements Dependent, MyServiceInterface {

	public Dependency<MyApplicationInterface> app = new Dependency<>(this, MyApplicationInterface.class);

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public String greets() {
		// TODO Auto-generated method stub
		return null;
	}

}
