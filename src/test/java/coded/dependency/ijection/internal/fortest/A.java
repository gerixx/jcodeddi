package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class A implements Dependent {

	// the wiring declaration, wiring is completed with Wiring#connect(...)
	public Dependency<B> b = new Dependency<>(this, B.class);
	public Dependency<C> c = new Dependency<>(this, C.class);

}
