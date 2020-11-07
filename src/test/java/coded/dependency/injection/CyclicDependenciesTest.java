package coded.dependency.injection;

import org.junit.Test;

import coded.dependency.injection.Injector;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.internal.fortest.AtoB;
import coded.dependency.injection.internal.fortest.BtoA;
import coded.dependency.injection.internal.fortest.MainWithCycle;
import coded.dependency.injection.internal.fortest.MyApplicationInterface;
import coded.dependency.injection.internal.fortest.MyAppToService;
import coded.dependency.injection.internal.fortest.MyServiceInterface;
import coded.dependency.injection.internal.fortest.MyServiceToApp;

public class CyclicDependenciesTest {

	@Test(expected = CyclicDependencyException.class)
	public void testBidirectionalDependency() throws Exception {
		Injector injector = Injector.getContext("app")
			.defineConstruction(AtoB.class, AtoB::new)
			.defineConstruction(BtoA.class, BtoA::new);
		injector.makeBeans(AtoB.class);
	}

	@Test(expected = CyclicDependencyException.class)
	public void testBidirectionalDependencyWithInterfaces() throws Exception {
		Injector injector = Injector.getContext("app")
			.defineConstruction(MyServiceInterface.class, MyServiceToApp::new)
			.defineConstruction(MyApplicationInterface.class, MyAppToService::new);
		injector.makeBeans(MyApplicationInterface.class);
	}

	@Test(expected = CyclicDependencyException.class)
	public void testDeepCyclicDependency() throws Exception {
		Injector.getContext("app")
			.makeBeans(MainWithCycle.class);
	}
}
