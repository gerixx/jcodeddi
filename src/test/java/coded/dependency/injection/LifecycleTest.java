package coded.dependency.injection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coded.dependency.injection.internal.fortest.Interface1;
import coded.dependency.injection.internal.fortest.Interface1And2Impl;
import coded.dependency.injection.internal.fortest.Interface1Dependent;
import coded.dependency.injection.internal.fortest.Interface2;
import coded.dependency.injection.internal.fortest.Interface2Dependent;

public class LifecycleTest extends TestBase {

	@Test
	public void testStartStopOrder() throws Exception {
		Injector injector = Injector.getContext("myapp");
		injector.defineConstruction(MyApp.class, MyAppImpl::new)
			.defineConstruction(MyService.class, MyServiceImpl::new)
			.defineConstruction(HelperProcessStarter.class, HelperProcessStarter::new)
			.makeBeans(MyApp.class)
			.start();

		assertTrue(injector.getBean(HelperProcessStarter.class)
			.isRunning());
		assertTrue(injector.getBean(MyApp.class)
			.isRunning());
		assertTrue(injector.getBean(MyService.class)
			.isRunning());

		injector.stop();

		assertFalse(injector.getBean(HelperProcessStarter.class)
			.isRunning());
		assertFalse(injector.getBean(MyApp.class)
			.isRunning());
		assertFalse(injector.getBean(MyService.class)
			.isRunning());
	}

	@Test
	public void testLifecycleOfServiceImplementsMultipleInterfaces() {
		Injector injector = Injector.getContext("app");
		injector.defineConstruction(Interface1.class, Interface1And2Impl::new)
			.defineConstruction(Interface2.class, () -> injector.getBean(Interface1And2Impl.class))
			.makeBeans(Interface1Dependent.class)
			.makeBeans(Interface2Dependent.class)
			.start();

		assertTrue(injector.getBean(Interface1And2Impl.class)
			.isRunning());

		injector.stop();
		assertFalse(injector.getBean(Interface1And2Impl.class)
			.isRunning());
	}
}

interface MyApp extends Dependent, Lifecycle {
	public boolean isRunning();
}

interface MyService extends Lifecycle {
	public boolean isRunning();

	public String greets();
}

class MyServiceImpl implements Dependent, MyService {

	HelperProcessStarter extSvc = new Dependency<>(this, HelperProcessStarter.class).get();

	private boolean isRunning;

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}

		if (!extSvc.isRunning()) {
			throw new IllegalStateException("HelperProcess must run");
		}

		isRunning = true;
		System.out.println("MyService started.");
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}

		if (!extSvc.isRunning()) {
			throw new IllegalStateException("HelperProcess must run");
		}

		isRunning = false;
		System.out.println("MyService stopped.");
	}

	@Override
	public String greets() {
		return "hello, MyService is ready to go";
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}

class MyAppImpl implements MyApp {
	private boolean isRunning;

	MyService svc = new Dependency<>(this, MyService.class).get();

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}
		if (!svc.isRunning()) {
			throw new IllegalStateException("svc is not running");
		}
		isRunning = true;
		String svcGreets = svc.greets();
		System.out.println("MyApp started, greets from svc: " + svcGreets);
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}
		isRunning = false;
		System.out.println("MyApp stopped");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}

class HelperProcessStarter implements Lifecycle {
	private boolean isRunning;

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}
		isRunning = true;
		System.out.println("SomeHelperProcess is started");
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}
		isRunning = false;
		System.out.println("SomeHelperProcess stopped");
	}

	public boolean isRunning() {
		return isRunning;
	}
}
