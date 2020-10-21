package coded.dependency.ijection.internal.fortest;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class AtoB implements Dependent {

	public Dependency<BtoA> btoa = new Dependency<>(this, BtoA.class);

}
