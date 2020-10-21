package coded.dependency.ijection;

import org.junit.Test;

import coded.dependency.ijection.internal.fortest.AtoB;
import coded.dependency.ijection.internal.fortest.BtoA;
import coded.dependency.ijection.internal.fortest.MyAppInterface;
import coded.dependency.ijection.internal.fortest.MyAppToService;
import coded.dependency.ijection.internal.fortest.MyServiceInterface;
import coded.dependency.ijection.internal.fortest.MyServiceToApp;
import coded.dependency.injection.RecursiveDependencyException;
import coded.dependency.injection.Wiring;

public class RecursiveDependenciesTest {

	@Test(expected = RecursiveDependencyException.class)
	public void testBidirectionalDependency() throws Exception {
		Wiring injector = Wiring.getContext("app")
			.defineConstruction(AtoB.class, AtoB::new)
			.defineConstruction(BtoA.class, BtoA::new);
		injector.connectAll(AtoB.class);
	}

	@Test(expected = RecursiveDependencyException.class)
	public void testBidirectionalDependencyWithInterfaces() throws Exception {
		Wiring injector = Wiring.getContext("app")
			.defineConstruction(MyServiceInterface.class, MyServiceToApp::new)
			.defineConstruction(MyAppInterface.class, MyAppToService::new);
		injector.connectAll(MyAppInterface.class);
	}
}
