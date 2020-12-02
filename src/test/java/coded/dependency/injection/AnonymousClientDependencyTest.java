package coded.dependency.injection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coded.dependency.injection.exception.ContextMismatchException;
import coded.dependency.injection.internal._WiringHelper;
import coded.dependency.injection.internal.fortest.A;
import coded.dependency.injection.internal.fortest.B;
import coded.dependency.injection.internal.fortest.C;
import coded.dependency.injection.internal.fortest.MyAnonymousApp;
import coded.dependency.injection.internal.fortest.MyAnonymousContextConflict_1;
import coded.dependency.injection.internal.fortest.MyAnonymousContextConflict_2;

public class AnonymousClientDependencyTest extends TestBase {

	@Test
	public void testAnonymousClient() {
		MyAnonymousApp app = new MyAnonymousApp();

		assertNull(Injector.getContext(MyAnonymousApp.APPCONTEXT)
			.getBean(MyAnonymousApp.class));

		A a = app.a.get();
		B b = a.b.get();
		assertTrue(a == Injector.getContext(MyAnonymousApp.APPCONTEXT)
			.getBean(A.class));
		assertTrue(a.c.get() == Injector.getContext(MyAnonymousApp.APPCONTEXT)
			.getBean(C.class));
		assertTrue(b == Injector.getContext(MyAnonymousApp.APPCONTEXT)
			.getBean(B.class));
		assertNotNull(_WiringHelper.getContext(MyAnonymousApp.APPCONTEXT));

		app.start();
	}

	@Test(expected = ContextMismatchException.class)
	public void testContextConflictException_makeBeans() {
		Injector.getContext("app")
			.makeBeans(MyAnonymousContextConflict_1.class);
	}

	@Test(expected = ContextMismatchException.class)
	public void testContextConflictException_anonymousClient_1() {
		new MyAnonymousContextConflict_2();
	}

	@Test(expected = ContextMismatchException.class)
	public void testContextConflictException_anonymousClient_2() {
		Injector.getContext("app")
			.makeBeans(MyAnonymousContextConflict_2.class);
	}
}
