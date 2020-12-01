package coded.dependency.injection;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.internal.fortest.AtoB;
import coded.dependency.injection.internal.fortest.BtoA;
import coded.dependency.injection.internal.fortest.MainWithCycle;
import coded.dependency.injection.internal.fortest.MyAppToService;
import coded.dependency.injection.internal.fortest.MyApplicationInterface;
import coded.dependency.injection.internal.fortest.MyServiceInterface;
import coded.dependency.injection.internal.fortest.MyServiceToApp;

public class CyclicDependenciesTest extends TestBase {

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

	private String EXPECTED_LOG = "[INFO] injector 'app': Make beans for dependent MainWithCycle (coded.dependency.injection.internal.fortest.MainWithCycle) ... - thread: main (CyclicDependenciesTest.java:57)\n"
			+ "[ERROR] injector 'app': Cyclic dependency to MainWithCycle (coded.dependency.injection.internal.fortest.MainWithCycle) - thread: main (MainSvc2.java:8)\n"
			+ "[ERROR] injector 'app': Cyclic dependency to MainWithCycle (coded.dependency.injection.internal.fortest.MainWithCycle) - thread: main (MainSvc1.java:8)\n"
			+ "[ERROR] injector 'app': Cyclic dependency to MainWithCycle (coded.dependency.injection.internal.fortest.MainWithCycle) - thread: main (MainWithCycle.java:8)"
			+ "";

	@Test
	public void testDeepCyclicDependencyLog() throws Exception {
		StringWriter logTarget = new StringWriter();
		try {
			Injector.getContext("app")
				.setLogger(new _LogBindingAdapterCapture(new PrintWriter(logTarget)))
				.makeBeans(MainWithCycle.class);
		} catch (Exception e) {
			// expected
		}

		System.out.println(logTarget.toString());

		// check log
		String logResult = Arrays.stream(logTarget.toString()
			.split("\\n"))
			.map(line -> cutDate(line).replace("\r", ""))
			.collect(Collectors.joining("\n"));
		assertEquals(EXPECTED_LOG, logResult);
	}

	private String cutDate(String line) {
		return line.substring(line.indexOf('['));
	}
}
