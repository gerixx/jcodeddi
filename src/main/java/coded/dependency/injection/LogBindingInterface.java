package coded.dependency.injection;

import java.util.function.Supplier;

/**
 * Interface for logging used by {@link Injector}, see also default
 * implementation {@link LogBindingAdapter}. To disable logging set the logger
 * to null with {@link Injector#setLogger(LogBindingInterface)}.
 * 
 */
public interface LogBindingInterface {

	void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier);

	void error(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier, Throwable t);

	void info(String contextName, StackTraceElement[] stack, Supplier<String> msgSupplier);
}
