package coded.dependency.injection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LifecycleTest {

	@Test
	public void test() throws Exception {
		Injector injector = Injector.getContext("myapp");
		injector.defineConstruction(MyApp.class, MyAppImpl::new)
			.defineConstruction(MyService.class, MyServiceImpl::new)
			.makeBeans(MyApp.class)
			.start();

		assertTrue(injector.getBean(MyApp.class)
			.isRunning());
		assertTrue(injector.getBean(MyService.class)
			.isRunning());

		injector.stop();

		assertFalse(injector.getBean(MyApp.class)
			.isRunning());
		assertFalse(injector.getBean(MyService.class)
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
