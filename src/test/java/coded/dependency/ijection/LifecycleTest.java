package coded.dependency.ijection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.Lifecycle;
import coded.dependency.injection.Wiring;

public class LifecycleTest {

	@Test
	public void test() throws Exception {
		Wiring injector = Wiring.getContext("myapp");
		injector.defineConstruction(MyApp.class, MyAppImpl::new)
			.defineConstruction(MyService.class, MyServiceImpl::new)
			.connectAll(MyApp.class)
			.start();

		assertTrue(injector.get(MyApp.class)
			.isRunning());
		assertTrue(injector.get(MyService.class)
			.isRunning());

		injector.stop();

		assertFalse(injector.get(MyApp.class)
			.isRunning());
		assertFalse(injector.get(MyService.class)
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

class MyServiceImpl implements MyService {
	private boolean isRunning;

	@Override
	public void start() {
		isRunning = true;
		System.out.println("MyService started.");
	}

	@Override
	public void stop() {
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

	Dependency<MyService> svc = new Dependency<>(this, MyService.class);

	@Override
	public void start() {
		isRunning = true;
		String svcGreets = svc.get()
			.greets();
		System.out.println("MyApp started, greets from svc: " + svcGreets);
	}

	@Override
	public void stop() {
		isRunning = false;
		System.out.println("MyApp stopped");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
