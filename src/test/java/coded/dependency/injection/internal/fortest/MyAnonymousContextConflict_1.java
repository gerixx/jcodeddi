package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyAnonymousContextConflict_1 implements Dependent {

	private Dependency<MyAnonymousApp> dep = new Dependency<>(this, MyAnonymousApp.class);
}
