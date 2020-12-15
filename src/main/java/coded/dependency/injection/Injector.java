package coded.dependency.injection;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.internal._WiringHelper;

/**
 * The coded injection API. Create or access a named injector using
 * {@link #getContext(String)}.
 * 
 */
public interface Injector {

	/**
	 * Retrieves the named injector. A new one is created if needed. Use this to
	 * configure and to inject your dependencies. Optionally it can be used to start
	 * and stop beans using the {@link Lifecycle} interface or
	 * {@link #defineStartStop(Class, Consumer, Consumer)}.
	 * 
	 * @param contextName identifier for the named injector
	 * @return the injector
	 */
	public static Injector getContext(String contextName) {
		return (Injector) _WiringHelper.getOrCreateContext(contextName);
	}

	public static String[] getContextNames() {
		return _WiringHelper.getContextNames();
	}

	/**
	 * Removes all injectors from the injection provider. Can be used to free all
	 * internally used memory. Use this carefully! Every subsequent invocation
	 * {@link #getContext(String)} would create a new injector instance, also for a
	 * previously used context name.
	 */
	public static void removeAll() {
		_WiringHelper.removeAll();
	}

	/**
	 * @return application context (injector) name
	 */
	String getName();

	/**
	 * Set your own log target by implementing {@link LogBindingInterface}. Default
	 * implementation is {@link LogBindingAdapter} writing logs to
	 * {@link System#out}. Set logger to null to disable log outputs.
	 */
	Injector setLogger(LogBindingInterface logger);

	/**
	 * Optional, defines the supplier of the given class, otherwise the default
	 * constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	<T> Injector defineConstruction(Class<? super T> clz, Supplier<? super T> construction);

	/**
	 * Optional, defines the consumer for the given class instance (the bean) which
	 * is executed on {@link Injector#start()}. See also interface {@link Lifecycle}
	 * which can be used alternatively.
	 */
	<T> Injector defineStart(Class<? super T> clz, Consumer<? super T> start);

	/**
	 * Optional, defines the consumer for the given class instance (the bean) which
	 * is executed on {@link Injector#stop()}. See also interface {@link Lifecycle}
	 * which can be used alternatively.
	 */
	<T> Injector defineStop(Class<? super T> clz, Consumer<? super T> stop);

	/**
	 * Optional, for convenience, it combines {@link #defineStart(Class, Consumer)}
	 * and {@link #defineStop(Class, Consumer)}
	 */
	<T> Injector defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop);

	/**
	 * Creates dependency objects (the beans) and wires them up recursively. Defined
	 * construction supplier or no-argument constructors are invoked to create beans
	 * if not created yet, see also {@link #defineConstruction(Class, Supplier)}.
	 * Beans are treated as 'singletons' within an injector. Multiple connects of
	 * classes are ignored.
	 * 
	 * @param <T>
	 * @param classDependent class to begin with recursive wiring
	 * @return injector
	 * @throws Exception
	 */
	<T extends Dependent> Injector makeBeans(Class<T> classDependent);

	/**
	 * Runs for all beans its start method if it was defined by
	 * {@link #defineStart(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	Injector start();

	/**
	 * Runs for all beans its stop method if it was defined by
	 * {@link #defineStop(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	Injector stop();

	/**
	 * Returns the bean for the given class or null if it does not exist. Any bean
	 * created by {@link #makeBeans(Class)} can be accessed, including service beans
	 * that were created by {@link Dependency} members of {@link Dependent}s.
	 * 
	 * @param <T> bean type
	 * @param clz bean class
	 * @return bean or null
	 */
	<T> T getBean(Class<T> clz);

	/**
	 * Prints the dependency tree(s) to System.out.
	 */
	Injector print();

	/**
	 * Print the dependency tree(s) to the given PrintWriter.
	 * 
	 * @param out
	 */
	Injector print(PrintStream out);

	/**
	 * Removes this application context (injector instance) from the injector
	 * provider. Can be used to free internal memory if an injector is not needed
	 * anymore, for example when using an injector in a Servlet session and it is
	 * released. Use this carefully! After removal, invocation of
	 * {@link Injector#getContext(String)} with the same context name would create a
	 * new injector instance.
	 */
	Injector remove();
}
