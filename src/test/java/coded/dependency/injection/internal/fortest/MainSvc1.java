package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MainSvc1 implements Dependent {

	public Dependency<MainSvc2> svc = new Dependency<>(this, MainSvc2.class);
}
