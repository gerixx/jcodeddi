package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class Interface1Dependent implements Dependent {

	Interface1 svc = (new Dependency<>(this, Interface1.class)).get();

	public String getInfo() {
		return svc.getUsageInfoOfInterface1();
	}
}
