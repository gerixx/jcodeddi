package coded.dependency.injection;

import java.util.function.Supplier;

public interface LogBindingInterface {

	void error(Class<?> clz, String contextName, Supplier<String> msgSupplier);

	void error(Class<?> clz, String contextName, Supplier<String> msgSupplier, Throwable t);

	void info(Class<?> clz, String contextName, Supplier<String> msgSupplier);
}
