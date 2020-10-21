package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyServiceToApp implements Dependent, MyServiceInterface {

	public Dependency<MyAppInterface> app = new Dependency<>(this, MyAppInterface.class);

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public String greets() {
		// TODO Auto-generated method stub
		return null;
	}

}
