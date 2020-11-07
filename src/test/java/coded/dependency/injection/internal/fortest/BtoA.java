package coded.dependency.injection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class BtoA implements Dependent {

	public Dependency<AtoB> atob = new Dependency<>(this, AtoB.class);

}
