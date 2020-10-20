package coded.dependency.injection;

import java.util.function.Supplier;

public interface LogBindingInterface {

	void error(Class<?> clz, String contextName, Supplier<String> msg);

	void error(Class<?> clz, String contextName, Supplier<String> msg, Throwable t);

	void info(Class<?> clz, String contextName, Supplier<String> msg);
}
