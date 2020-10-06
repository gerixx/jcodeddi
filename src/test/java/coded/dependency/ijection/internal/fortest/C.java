package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class C implements Dependent {

	private Dependency<D> d = new Dependency<>(this, D.class);

	public C() {
	}

	public C(int i, int j) {
		System.out.println("new C(i, j)");
	}

	public String world() {
		return "world";
	}
}
