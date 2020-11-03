package coded.dependency.injection.internal;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.Dependent;
import coded.dependency.injection.Lifecycle;
import coded.dependency.injection.LogBindingInterface;
import coded.dependency.injection.Wiring;

public interface _WiringInterface {

	/**
	 * Set your own log target by implementing {@link LogBindingInterface}. Default
	 * log target is System.out. Set null to disable logs.
	 */
	_WiringInterface setLogger(LogBindingInterface logger);

	/**
	 * Optional, defines the supplier of the given class, otherwise the default
	 * constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	<T> _WiringInterface defineConstruction(Class<? super T> clz, Supplier<? super T> construction);

	/**
	 * Optional, defines the consumer for the given class instance (the bean) which
	 * is executed on {@link Wiring#start()}. See also interface {@link Lifecycle}
	 * which can be used alternatively.
	 */
	<T> _WiringInterface defineStart(Class<? super T> clz, Consumer<? super T> start);

	/**
	 * Optional, defines the consumer for the given class instance (the bean) which
	 * is executed on {@link Wiring#stop()}. See also interface {@link Lifecycle}
	 * which can be used alternatively.
	 */
	<T> _WiringInterface defineStop(Class<? super T> clz, Consumer<? super T> stop);

	/**
	 * Optional, for convenience, it combines {@link #defineStart(Class, Consumer)}
	 * and {@link #defineStop(Class, Consumer)}
	 */
	<T> _WiringInterface defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop);

	/**
	 * Creates dependency objects (the beans) and wires them up recursively. Defined
	 * construction supplier or no-argument constructors are invoked to create beans
	 * if not created yet, see also {@link #defineConstruction(Class, Supplier)}.
	 * Beans are treated as 'singletons' within the Wiring context. Multiple
	 * connects of classes are ignored.
	 * 
	 * @param <T>
	 * @param classDependent class to begin with recursive wiring
	 * @return injector
	 * @throws Exception
	 */
	<T extends Dependent> _WiringInterface connectAll(Class<T> classDependent);

	/**
	 * Runs for all beans its start method if it was defined by
	 * {@link #defineStart(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	_WiringInterface start();

	/**
	 * Runs for all beans its stop method if it was defined by
	 * {@link #defineStop(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	_WiringInterface stop();

	/**
	 * Returns the bean for the given class or null if it is not existing.
	 * 
	 * @param <T> bean type
	 * @param clz class
	 * @return bean or null
	 */
	<T> T get(Class<T> clz);

	/**
	 * Prints the dependency tree(s) to System.out.
	 */
	void print();

	/**
	 * Print the dependency tree(s) to the given PrintWriter.
	 * 
	 * @param out
	 */
	void print(PrintStream out);
}
