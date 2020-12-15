package coded.dependency.injection;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.After;
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

	@After
	public void after() {
		Injector.removeAll();
	}

	protected String cutDateOfLine(String line) {
		return line.substring(line.indexOf('['));
	}

	protected String cutDate(String lines) {
		return Arrays.stream(lines.split("\\n"))
			.map(line -> cutDateOfLine(line).replace("\r", ""))
			.collect(Collectors.joining("\n"));
	}

	protected String replaceInMsAndLineNumber(String lines) {
		return lines.replaceAll("in \\d+ms", "in Xms")
			.replaceAll(".java:\\d+", ".java:X");
	}

}
