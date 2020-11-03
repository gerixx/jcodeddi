package coded.dependency.ijection;

import org.junit.Test;

import coded.dependency.ijection.internal.fortest.AtoB;
import coded.dependency.ijection.internal.fortest.BtoA;
import coded.dependency.ijection.internal.fortest.MainWithCycle;
import coded.dependency.ijection.internal.fortest.MyAppInterface;
import coded.dependency.ijection.internal.fortest.MyAppToService;
import coded.dependency.ijection.internal.fortest.MyServiceInterface;
import coded.dependency.ijection.internal.fortest.MyServiceToApp;
import coded.dependency.injection.Injector;
import coded.dependency.injection.exception.CyclicDependencyException;

public class CyclicDependenciesTest {

	@Test(expected = CyclicDependencyException.class)
	public void testBidirectionalDependency() throws Exception {
		Injector injector = Injector.getContext("app")
			.defineConstruction(AtoB.class, AtoB::new)
			.defineConstruction(BtoA.class, BtoA::new);
		injector.connectAll(AtoB.class);
	}

	@Test(expected = CyclicDependencyException.class)
	public void testBidirectionalDependencyWithInterfaces() throws Exception {
		Injector injector = Injector.getContext("app")
			.defineConstruction(MyServiceInterface.class, MyServiceToApp::new)
			.defineConstruction(MyAppInterface.class, MyAppToService::new);
		injector.connectAll(MyAppInterface.class);
	}

	@Test(expected = CyclicDependencyException.class)
	public void testDeepCyclicDependency() throws Exception {
		Injector.getContext("app")
			.connectAll(MainWithCycle.class);
	}
}
