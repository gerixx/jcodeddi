package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MainWithCycle implements Dependent {

	public Dependency<MainSvc1> svc = new Dependency<>(this, MainSvc1.class);

}
