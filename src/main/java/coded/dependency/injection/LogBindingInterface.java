package coded.dependency.injection;

import java.util.function.Supplier;

public interface LogBindingInterface {

	void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier);

	void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier, Throwable t);

	void info(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier);
}
