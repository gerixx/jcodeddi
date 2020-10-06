package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class D implements Dependent {

	private Dependency<B> b = new Dependency<>(this, B.class);
}
