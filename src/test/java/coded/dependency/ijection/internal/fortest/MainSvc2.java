package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MainSvc2 implements Dependent {

	public Dependency<MainWithCycle> svc = new Dependency<>(this, MainWithCycle.class);
}
