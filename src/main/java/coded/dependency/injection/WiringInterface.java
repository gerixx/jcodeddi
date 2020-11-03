package coded.dependency.injection;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface WiringInterface {

	WiringInterface setLogger(LogBindingInterface logger);

	/**
	 * Optional, otherwise default constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	<T> WiringInterface defineConstruction(Class<? super T> clz, Supplier<? super T> construction);

	/**
	 * Optional
	 */
	<T> WiringInterface defineStart(Class<? super T> clz, Consumer<? super T> start);

	/**
	 * Optional
	 */
	<T> WiringInterface defineStop(Class<? super T> clz, Consumer<? super T> stop);

	/**
	 * Optional
	 */
	<T> WiringInterface defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop);

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
	<T extends Dependent> WiringInterface connectAll(Class<T> classDependent);

	WiringInterface start();

	WiringInterface stop();

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
