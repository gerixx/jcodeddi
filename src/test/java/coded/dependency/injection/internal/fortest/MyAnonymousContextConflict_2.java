package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyAnonymousContextConflict_2 implements Dependent {

	private Dependency<MyAnonymousApp> dep = new Dependency<>("conflicting_anonymous_client", this,
			MyAnonymousApp.class);
}
