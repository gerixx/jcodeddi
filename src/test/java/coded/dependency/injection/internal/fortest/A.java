package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class A implements Dependent {

	public Dependency<B> b = new Dependency<>(this, B.class);
	public Dependency<C> c = new Dependency<>(this, C.class);

	public String greets() {
		return "hello, I'm an A.";
	}
}
