package coded.dependency.injection;

import java.util.function.Supplier;

public interface LogBindingInterface {

	void error(Class<?> clz, Supplier<String> msg);

	void error(Class<?> clz, Supplier<String> msg, Throwable t);

	void info(Class<?> clz, Supplier<String> msg);
}
