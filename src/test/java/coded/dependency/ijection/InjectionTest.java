package coded.dependency.ijection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;

import coded.dependency.ijection.internal.fortest.A;
import coded.dependency.ijection.internal.fortest.B;
import coded.dependency.ijection.internal.fortest.C;
import coded.dependency.ijection.internal.fortest.D;
import coded.dependency.ijection.internal.fortest.MyAppImpl;
import coded.dependency.ijection.internal.fortest.MyAppInterface;
import coded.dependency.ijection.internal.fortest.MyServiceImpl;
import coded.dependency.ijection.internal.fortest.MyServiceInterface;
import coded.dependency.injection.Injector;
import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.DependencyCreationException;
import coded.dependency.injection.internal._WiringDoer;
import coded.dependency.injection.internal._WiringHelper;

public class InjectionTest {

	@After
	public void after() {
		Injector.resetAll();
	}

	/**
	 * Connect beans: <br>
	 * A -> B <br>
	 * A -> C -> D
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMostSimple() throws Exception {
		Injector injector = Injector.getContext("main");
		A a = injector.makeBeans(A.class)
			.getBean(A.class);

		B b = a.b.get();
		assertNotNull(b);
		assertEquals("hello", b.hello());

		C c = a.c.get();
		assertNotNull(c);
		assertEquals("world", c.world());

		assertTrue(injector.getBean(A.class) == a);
		assertTrue(injector.getBean(B.class) == b);
		assertTrue(injector.getBean(C.class) == c);

		injector.print();
	}

	/**
	 * Connect beans to instances: <br>
	 * A -> b <br>
	 * A -> c
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConnectToBySupplier() throws Exception {
		A a = Injector.getContext("main")
			.defineConstruction(B.class, B::new)
			.defineConstruction(C.class, () -> new C(1, 2))
			.makeBeans(A.class)
			.getBean(A.class);

		B b = a.b.get();
		assertNotNull(b);
		assertEquals("hello", b.hello());

		C c = a.c.get();
		assertNotNull(c);
		assertEquals("world", c.world());
	}

	@Test
	public void testConnectInterfaces() throws Exception {
		Injector injector = Injector.getContext("app");

		MyAppInterface app = injector.defineConstruction(MyServiceInterface.class, MyServiceImpl::new)
			.makeBeans(MyAppImpl.class)
			.getBean(MyAppImpl.class);

		String greets = app.start();
		assertEquals("greets from my service", greets);

		MyServiceInterface beanAsInterface = injector.getBean(MyServiceInterface.class);
		assertEquals("greets from my service", beanAsInterface.greets());

		MyServiceImpl beanAsImplementation = injector.getBean(MyServiceImpl.class);
		assertEquals("greets from my service", beanAsImplementation.greets());
	}

	String greets = null;

	@Test
	public void testStartStop() throws Exception {
		Injector.getContext("app")
			.defineConstruction(MyServiceInterface.class, MyServiceImpl::new)
			.defineStartStop(MyAppImpl.class, app -> greets = app.start(), app -> app.stop())
			.defineStartStop(MyServiceImpl.class, svc -> svc.initialize(), svc -> svc.destroy())
			.makeBeans(MyAppImpl.class)
			.start();

		Injector injector = Injector.getContext("app");
		injector.print();

		// then
		assertTrue(injector.getBean(MyAppImpl.class)
			.isStarted());
		assertTrue(injector.getBean(MyServiceImpl.class)
			.isInitialized());
		assertEquals("greets from my service", greets);
		assertTrue(injector.getBean(B.class).isStarted);

		// when stop then
		injector.stop();
		assertTrue(injector.getBean(MyAppImpl.class)
			.isStopped());
		assertTrue(injector.getBean(MyServiceImpl.class)
			.isStopped());
		assertFalse(injector.getBean(B.class).isStarted);
	}

	@Test(expected = ConstructionMissingException.class)
	public void testMissingInterfaceConstruction() throws Exception {
		Injector.getContext("app")
			.defineConstruction(MyAppImpl.class, MyAppImpl::new)
			.makeBeans(MyAppImpl.class);
	}

	@Test(expected = BeanOutOfContextCreationException.class)
	public void testBeanOutOfContextCreation() {
		new A();
	}

	@Test(expected = DependencyCreationException.class)
	public void testInvalidCreationThreadContext() {
		Injector.getContext("app")
			.defineConstruction(D.class, this::createD_inNewThread)
			.makeBeans(A.class);
	}

	@Test
	public void testReset() {
		Injector injector = Injector.getContext("main");
		A a = injector.makeBeans(A.class)
			.getBean(A.class);
		B b = a.b.get();

		assertNotNull(_WiringDoer.getContext("main"));
		assertNotNull(_WiringHelper.getContext("main"));

		injector.reset();
		assertNull(_WiringDoer.getContext("main"));
		assertNull(_WiringHelper.getContext("main"));

		assertNotNull(b);
		assertEquals("hello", b.hello());
	}

	private ExecutorService exec = Executors.newSingleThreadExecutor();

	private D createD_inNewThread() {
		try {
			return exec.submit(D::new)
				.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
