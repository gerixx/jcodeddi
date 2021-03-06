package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class A2 implements Dependent {

	public Dependency<B> b = new Dependency<>(this, B.class);
	public Dependency<C> c = new Dependency<>(this, C.class);
}
