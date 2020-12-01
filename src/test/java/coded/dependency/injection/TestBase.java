package coded.dependency.injection;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestBase {

	@Rule
	public TestWatcher watchman = new TestWatcher() {
			@Override
			protected void starting(Description description) {
				super.starting(description);
				System.out.printf("%ntest: %s#%s()%n", description.getTestClass()
					.getName(), description.getMethodName());
			}
		};

}
