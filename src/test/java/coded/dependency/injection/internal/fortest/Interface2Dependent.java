package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class Interface2Dependent implements Dependent {

	Interface2 svc = (new Dependency<>(this, Interface2.class)).get();

	public String getInfo() {
		return svc.getUsageInfoOfInterface2();
	}
}
