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
	public void error(Class<?> clz, String contextName, Supplier<String> msgSupplier) {
		out.printf("%s [ERROR] injector '%s' %s - thread: %s - %s%n", new Date(), contextName, msgSupplier.get(),
				Thread.currentThread()
					.getName(),
				clz.getName());
	}

	@Override
	public void error(Class<?> clz, String contextName, Supplier<String> msgSupplier, Throwable t) {
		out.printf("%s [ERROR] injector '%s' %s - thread: %s - %s%n%s%n", new Date(), contextName, msgSupplier.get(),
				Thread.currentThread()
					.getName(),
				clz.getName(), throwableToString(t));
	}

	@Override
	public void info(Class<?> clz, String contextName, Supplier<String> msgSupplier) {
		out.printf("%s [INFO] injector '%s' %s - thread: %s - %s%n", new Date(), contextName, msgSupplier.get(),
				Thread.currentThread()
					.getName(),
				clz.getName());
	}

	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
