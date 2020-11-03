package coded.dependency.injection.internal;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.Dependent;
import coded.dependency.injection.LogBindingInterface;

public interface _WiringInterface {

	_WiringInterface setLogger(LogBindingInterface logger);

	/**
	 * Optional, otherwise default constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	<T> _WiringInterface defineConstruction(Class<? super T> clz, Supplier<? super T> construction);

	/**
	 * Optional
	 */
	<T> _WiringInterface defineStart(Class<? super T> clz, Consumer<? super T> start);

	/**
	 * Optional
	 */
	<T> _WiringInterface defineStop(Class<? super T> clz, Consumer<? super T> stop);

	/**
	 * Optional
	 */
	<T> _WiringInterface defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop);

	/**
	 * Creates dependency objects and wires them up recursively. Defined
	 * construction supplier or no-argument constructors are invoked to create
	 * objects if not created yet. Objects are treated as 'singletons' within the
	 * Wiring context. Multiple connects of classes are ignored.
	 * 
	 * @param <T>
	 * @param classDependent class to begin with recursive wiring
	 * @return injector
	 * @throws Exception
	 */
	<T extends Dependent> _WiringInterface connectAll(Class<T> classDependent);

	_WiringInterface start();

	_WiringInterface stop();

	/**
	 * Returns singleton instance of given class or null if not existing.
	 * 
	 * @param <T> dedicated type
	 * @param clz class
	 * @return singleton or null
	 */
	<T> T get(Class<T> clz);

	void print();

	void print(PrintStream out);
}
