package coded.dependency.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.DependencyCreationException;
import coded.dependency.injection.exception.MakeBeansException;
import coded.dependency.injection.internal._LogBindingAdapterDebug;
import coded.dependency.injection.internal._WiringHelper;
import coded.dependency.injection.internal.fortest.A;
import coded.dependency.injection.internal.fortest.A2;
import coded.dependency.injection.internal.fortest.B;
import coded.dependency.injection.internal.fortest.C;
import coded.dependency.injection.internal.fortest.D;
import coded.dependency.injection.internal.fortest.Interface1;
import coded.dependency.injection.internal.fortest.Interface1And2Impl;
import coded.dependency.injection.internal.fortest.Interface1Dependent;
import coded.dependency.injection.internal.fortest.Interface2;
import coded.dependency.injection.internal.fortest.Interface2Dependent;
import coded.dependency.injection.internal.fortest.MyApplicationImpl;
import coded.dependency.injection.internal.fortest.MyApplicationInterface;
import coded.dependency.injection.internal.fortest.MyServiceImplementation;
import coded.dependency.injection.internal.fortest.MyServiceInterface;

public class InjectionTest extends TestBase {

	private static final String EXPECTED_DEFAULT_LOG = "[INFO] injector 'app': Make beans for dependent D (coded.dependency.injection.internal.fortest.D) ... - thread: main (InjectionTest.java:X)\n"
			+ "[INFO] injector 'app': Created B (coded.dependency.injection.internal.fortest.B) using default consctructor in Xms. - thread: main (D.java:X)\n"
			+ "[INFO] injector 'app': Injected D -> B ('coded.dependency.injection.internal.fortest.B' into the dependent 'coded.dependency.injection.internal.fortest.D'). - thread: main (D.java:X)\n"
			+ "[INFO] injector 'app': Created D (coded.dependency.injection.internal.fortest.D) using default consctructor in Xms. - thread: main (InjectionTest.java:X)\n"
			+ "[INFO] injector 'app': Make beans finished in Xms. - thread: main (InjectionTest.java:X)";

	private PrintStream sysOut;

	@Before
	public void beforeInjectionTest() {
		sysOut = System.out;
	}

	@After
	public void afterInjectionTest() {
		System.setOut(sysOut);
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

	private int newCntB = 0, newCntC = 0, newCntD = 0;

	/**
	 * A -> B, C and A2 -> B, C and C -> D
	 */
	@Test
	public void testMultipleServiceBeanDependencies() {
		Injector.getContext("multi")
			.defineConstruction(B.class, () -> {
				newCntB++;
				return new B();
			})
			.defineConstruction(C.class, () -> {
				newCntC++;
				return new C();
			})
			.defineConstruction(D.class, () -> {
				newCntD++;
				return new D();
			})
			.makeBeans(A.class)
			.makeBeans(A2.class);

		Injector.getContext("multi")
			.print();

		// then
		assertEquals(1, newCntB);
		assertEquals(1, newCntC);
		assertEquals(1, newCntD);
	}

	@Test
	public void testContextNames() {
		Injector.getContext("ctx1");
		Injector.getContext("ctx2");

		assertEquals("[ctx1, ctx2]", Arrays.toString(Injector.getContextNames()));

		Injector.removeAll();
		assertEquals(0, Injector.getContextNames().length);
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

		MyApplicationInterface app = injector.defineConstruction(MyServiceInterface.class, MyServiceImplementation::new)
			.makeBeans(MyApplicationImpl.class)
			.getBean(MyApplicationImpl.class);

		String greets = app.start();
		assertEquals("greets from my service", greets);

		MyServiceInterface beanAsInterface = injector.getBean(MyServiceInterface.class);
		assertEquals("greets from my service", beanAsInterface.greets());

		MyServiceImplementation beanAsImplementation = injector.getBean(MyServiceImplementation.class);
		assertEquals("greets from my service", beanAsImplementation.greets());
	}

	String greets = null;

	@Test
	public void testStartStop() throws Exception {
		Injector.getContext("app")
			.setLogger(new _LogBindingAdapterDebug())
			.defineConstruction(MyServiceInterface.class, MyServiceImplementation::new)
			.defineStartStop(MyApplicationImpl.class, app -> greets = app.start(), app -> app.stop())
			.defineStartStop(MyServiceImplementation.class, svc -> svc.initialize(), svc -> svc.destroy())
			.makeBeans(MyApplicationImpl.class)
			.start();

		Injector injector = Injector.getContext("app");
		injector.print();

		// then
		validateStartStopWorks(injector);
	}

	private void validateStartStopWorks(Injector injector) {
		assertTrue(injector.getBean(MyApplicationImpl.class)
			.isStarted());
		assertTrue(injector.getBean(MyServiceImplementation.class)
			.isInitialized());
		assertEquals("greets from my service", greets);
		assertTrue(injector.getBean(B.class).isStarted);

		// when stop then
		injector.stop();
		assertTrue(injector.getBean(MyApplicationImpl.class)
			.isStopped());
		assertTrue(injector.getBean(MyServiceImplementation.class)
			.isStopped());
		assertFalse(injector.getBean(B.class).isStarted);
	}

	@Test(expected = ConstructionMissingException.class)
	public void testMissingInterfaceConstruction() throws Exception {
		Injector.getContext("app")
			.defineConstruction(MyApplicationImpl.class, MyApplicationImpl::new)
			.makeBeans(MyApplicationImpl.class);
	}

	@Test(expected = BeanOutOfContextCreationException.class)
	public void testBeanOutOfContextCreation() {
		new A();
	}

	@Test
	public void testRemoveInjector() {
		Injector injector = Injector.getContext("main");
		A a = injector.makeBeans(A.class)
			.getBean(A.class);
		B b = a.b.get();

		assertNotNull(Injector.getContext("main"));
		assertTrue(a == Injector.getContext("main")
			.getBean(A.class));
		assertTrue(a.c.get() == Injector.getContext("main")
			.getBean(C.class));
		assertTrue(b == Injector.getContext("main")
			.getBean(B.class));
		assertNotNull(_WiringHelper.getContext("main"));

		// when
		injector.remove();

		// then
		Injector injector2 = Injector.getContext("main"); // a new injector is created
		assertEquals(injector.getName(), injector2.getName());
		assertNull(injector2.getBean(A.class));
		assertNull(_WiringHelper.getContext("main")
			.getDependencies(a));

		assertNotNull(b);
		assertEquals("hello", b.hello());
	}

	@Test
	public void testRemoveInjectorLifecycleStillWorking() {
		Injector injector = Injector.getContext("app")
			.defineConstruction(MyServiceInterface.class, MyServiceImplementation::new)
			.defineStartStop(MyApplicationImpl.class, app -> greets = app.start(), app -> app.stop())
			.defineStartStop(MyServiceImplementation.class, svc -> svc.initialize(), svc -> svc.destroy())
			.makeBeans(MyApplicationImpl.class);

		assertNotNull(Injector.getContext("app")
			.getBean(MyServiceInterface.class));

		// when
		injector.remove();

		// then
		Injector injector2 = Injector.getContext("app"); // a new injector is created
		assertEquals(injector.getName(), injector2.getName());
		assertNull(injector2.getBean(MyServiceInterface.class));

		// reference 'injector' is still working but injection provider does not refer
		// it anymore, it could be garbage collected
		injector.start();
		validateStartStopWorks(injector);
	}

	@Test(expected = MakeBeansException.class)
	public void testCannotMakeBeansWhenContextWasRemoved() {
		Injector.getContext("app")
			.remove()
			.makeBeans(MyApplicationImpl.class);
	}

	@Test(expected = DependencyCreationException.class)
	public void testInvalidCreationThreadContext() {
		Injector.getContext("app")
			.defineConstruction(D.class, this::createD_inNewThread)
			.makeBeans(A.class);
	}

	@Test
	public void testDefaultLogger() {
		ByteArrayOutputStream logTarget = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(logTarget);
		System.setOut(out);

		Injector.getContext("app")
			.makeBeans(D.class);

		assertEquals(EXPECTED_DEFAULT_LOG, replaceInMsAndLineNumber(cutDate(logTarget.toString())));
	}

	@Test
	public void testDisableLogger() {
		ByteArrayOutputStream logTarget = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(logTarget);
		System.setOut(out);

		Injector.getContext("app")
			.setLogger(null)
			.makeBeans(A.class);
		out.flush();

		assertEquals(0, logTarget.toByteArray().length);
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

	@Test
	public void testServiceImplementsMultipleInterfaces() {
		Injector injector = Injector.getContext("app");
		injector.defineConstruction(Interface1.class, Interface1And2Impl::new)
			.defineConstruction(Interface2.class, () -> injector.getBean(Interface1And2Impl.class))
			.makeBeans(Interface1Dependent.class)
			.makeBeans(Interface2Dependent.class);

		Interface1Dependent bean1 = injector.getBean(Interface1Dependent.class);
		Interface2Dependent bean2 = injector.getBean(Interface2Dependent.class);

		assertEquals("implementation of Interface1", bean1.getInfo());
		assertEquals("implementation of Interface2", bean2.getInfo());
	}
}
