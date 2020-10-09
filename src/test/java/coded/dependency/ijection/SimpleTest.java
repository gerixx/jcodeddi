package coded.dependency.ijection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import coded.dependency.ijection.internal.fortest.A;
import coded.dependency.ijection.internal.fortest.B;
import coded.dependency.ijection.internal.fortest.C;
import coded.dependency.ijection.internal.fortest.MyApp;
import coded.dependency.ijection.internal.fortest.MyAppImpl;
import coded.dependency.ijection.internal.fortest.MyService;
import coded.dependency.ijection.internal.fortest.MyServiceImpl;
import coded.dependency.injection.Wiring;

public class SimpleTest {

	@After
	public void after() {
		Wiring.resetAll();
	}

	/**
	 * Connect singletons: <br>
	 * A -> B <br>
	 * A -> C -> D
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMostSimple() throws Exception {
		Wiring injector = Wiring.getContext("main");
		A a = injector.connectAll(A.class)
			.get(A.class);

		B b = a.b.get();
		assertNotNull(b);
		assertEquals("hello", b.hello());

		C c = a.c.get();
		assertNotNull(c);
		assertEquals("world", c.world());

		injector.print();
	}

	/**
	 * Connect singleton to instances: <br>
	 * A -> b <br>
	 * A -> c
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConnectToBySupplier() throws Exception {
		A a = Wiring.getContext("main")
			.defineConstruction(B.class, B::new)
			.defineConstruction(C.class, () -> new C(1, 2))
			.connectAll(A.class)
			.get(A.class);

		B b = a.b.get();
		assertNotNull(b);
		assertEquals("hello", b.hello());

		C c = a.c.get();
		assertNotNull(c);
		assertEquals("world", c.world());
	}

	@Test
	public void testConnectInterfaces() throws Exception {
		MyApp app = Wiring.getContext("app")
			.defineConstruction(MyService.class, MyServiceImpl::new)
			.connectAll(MyAppImpl.class)
			.get(MyAppImpl.class);

		String greets = app.start();
		assertEquals("greets from my service", greets);
	}

	String greets = null;

	@Test
	public void testStartStop() throws Exception {
		Wiring.getContext("app")
			.defineConstruction(MyService.class, MyServiceImpl::new)
			.defineStartStop(MyAppImpl.class, app -> greets = app.start(), app -> app.stop())
			.defineStartStop(MyServiceImpl.class, svc -> svc.initialize(), svc -> svc.destroy())
			.connectAll(MyAppImpl.class)
			.start()
			.await();

		Wiring injector = Wiring.getContext("app");
		injector.print();

		// then
		assertTrue(injector.get(MyAppImpl.class)
			.isStarted());
		assertTrue(injector.get(MyServiceImpl.class)
			.isInitialized());
		assertEquals("greets from my service", greets);
		assertTrue(injector.get(B.class).isStarted);

		// when stop then
		injector.stop()
			.await();
		assertTrue(injector.get(MyAppImpl.class)
			.isStopped());
		assertTrue(injector.get(MyServiceImpl.class)
			.isStopped());
		assertFalse(injector.get(B.class).isStarted);
	}
}
