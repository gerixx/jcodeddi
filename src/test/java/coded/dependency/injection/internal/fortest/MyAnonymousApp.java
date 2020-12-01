package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyAnonymousApp implements Dependent {

	public static final String APPCONTEXT = "appcontext";

	public Dependency<A> a = new Dependency<>(APPCONTEXT, this, A.class);

	public void start() {
		System.out.println("MyAnonymousApp start...");
		System.out.println(a.get()
			.greets());
	}

}
