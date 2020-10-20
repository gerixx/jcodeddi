package coded.dependency.injection.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.Supplier;

import coded.dependency.injection.LogBindingInterface;

public class LogBindingAdapter implements LogBindingInterface {

	private PrintWriter out;

	public LogBindingAdapter(PrintWriter out) {
		this.out = out;
	}

	@Override
	public void error(Class<?> clz, String contextName, Supplier<String> msg) {
		out.printf("%s [ERROR] wiring '%s' %s (%s) - %s%n", new Date(), contextName, msg.get(), Thread.currentThread()
			.getName(), clz.getName());
	}

	@Override
	public void error(Class<?> clz, String contextName, Supplier<String> msg, Throwable t) {
		out.printf("%s [ERROR] wiring '%s' %s (thread: %s) - %s%n%s%n", new Date(), contextName, msg.get(),
				Thread.currentThread()
					.getName(),
				clz.getName(), throwableToString(t));
	}

	@Override
	public void info(Class<?> clz, String contextName, Supplier<String> msg) {
		out.printf("%s [INFO] wiring '%s' %s (%s) - %s%n", new Date(), contextName, msg.get(), Thread.currentThread()
			.getName(), clz.getName());
	}

	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
