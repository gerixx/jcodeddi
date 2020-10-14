package coded.dependency.injection.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.Supplier;

import coded.dependency.injection.LogBindingInterface;

public class LogDefaultSystemOut implements LogBindingInterface {

	@Override
	public void error(Class<?> clz, Supplier<String> msg) {
		System.out.printf("%s [ERROR] %s (%s) - %s%n", new Date(), msg.get(), Thread.currentThread()
			.getName(), clz.getName());
	}

	@Override
	public void error(Class<?> clz, Supplier<String> msg, Throwable t) {
		System.out.printf("%s [ERROR] %s (%s) - %s%n%s%n", new Date(), msg.get(), Thread.currentThread()
			.getName(), clz.getName(), throwableToString(t));
	}

	@Override
	public void info(Class<?> clz, Supplier<String> msg) {
		System.out.printf("%s [INFO] %s (%s) - %s%n", new Date(), msg.get(), Thread.currentThread()
			.getName(), clz.getName());
	}

	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
