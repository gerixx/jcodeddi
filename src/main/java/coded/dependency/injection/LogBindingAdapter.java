package coded.dependency.injection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.Supplier;

public class LogBindingAdapter implements LogBindingInterface {

	private PrintWriter out;

	public LogBindingAdapter(PrintWriter out) {
		this.out = out;
	}

	@Override
	public void error(Class<?> clz, String contextName, String fileName, int lineNumber, Supplier<String> msgSupplier) {
		out.printf("%s [ERROR] injector '%s' %s - thread: %s - %s (%s:%d)%n", new Date(), contextName,
				msgSupplier.get(), Thread.currentThread()
					.getName(),
				clz.getName(), fileName, lineNumber);
	}

	@Override
	public void error(Class<?> clz, String contextName, String fileName, int lineNumber, Supplier<String> msgSupplier,
			Throwable t) {
		out.printf("%s [ERROR] injector '%s' %s - thread: %s - %s (%s:%d)%n%s%n", new Date(), contextName,
				msgSupplier.get(), Thread.currentThread()
					.getName(),
				clz.getName(), fileName, lineNumber, throwableToString(t));
	}

	@Override
	public void info(Class<?> clz, String contextName, String fileName, int lineNumber, Supplier<String> msgSupplier) {
		out.printf("%s [INFO] injector '%s' %s - thread: %s - %s (%s:%d)%n", new Date(), contextName, msgSupplier.get(),
				Thread.currentThread()
					.getName(),
				clz.getName(), fileName, lineNumber);
	}

	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
