package coded.dependency.injection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.Supplier;

import coded.dependency.injection.internal._WiringHelper;

/**
 * Default log implementation, see {@link LogBindingInterface}.
 *
 */
public class LogBindingAdapter implements LogBindingInterface {

	private PrintWriter out;

	/**
	 * Override this to customize the log output stream. Default used by
	 * {@link Injector} is {@link System#out}.
	 * 
	 * @param out log outputs target
	 */
	public LogBindingAdapter(PrintWriter out) {
		this.out = out;
	}

	/**
	 * Retrieve first stack element that belongs to Injection API usage code.
	 * 
	 * @param stackTrace
	 * @return stack trace element using the current injection API
	 */
	protected StackTraceElement findUserCodeStackTraceElement(StackTraceElement[] stackTrace) {
		boolean injectionStackBegin = false;
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement elem = stackTrace[i];
			if (!injectionStackBegin && elem.getClassName()
				.equals(_WiringHelper.class.getName()) || elem.getClassName()
					.equals(Dependency.class.getName())) {
				injectionStackBegin = true;
			}
			if (injectionStackBegin && (!elem.getClassName()
				.startsWith(Injector.class.getPackageName()) || isInjectionTest(elem))) {
				return elem;
			}
		}
		return stackTrace[1];

	}

	private boolean isInjectionTest(StackTraceElement elem) {
		return elem.getClassName()
			.endsWith("Test")
				|| elem.getClassName()
					.contains(".fortest.")
				|| elem.getClassName()
					.contains(".example.");
	}

	@Override
	public void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier) {
		StackTraceElement stackTraceElement = findUserCodeStackTraceElement(new Throwable().getStackTrace());
		print("ERROR", contextName, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), msgSupplier);
	}

	@Override
	public void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier, Throwable t) {
		error(contextName, stack, msgSupplier);
		out.printf("%s%n", throwableToString(t));
		out.flush();
	}

	@Override
	public void info(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier) {
		StackTraceElement stackTraceElement = findUserCodeStackTraceElement(new Throwable().getStackTrace());
		print("INFO", contextName, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), msgSupplier);
	}

	protected void print(String level, String contextName, String fileName, int lineNumber,
			Supplier<String> msgSupplier) {
		out.printf("%s [%s] injector '%s': %s - thread: %s (%s:%d)%n", new Date(), level, contextName,
				msgSupplier.get(), Thread.currentThread()
					.getName(),
				fileName, lineNumber);
		out.flush();
	}

	protected String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
