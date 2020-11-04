package coded.dependency.injection;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.internal._WiringDoer;
import coded.dependency.injection.internal._WiringInterface;

/**
 * The coded injection API. Create or access a named injector using
 * {@link #getContext(String)}.
 * 
 */
public class Injector implements _WiringInterface {

	private _WiringInterface delegate;

	private Injector(_WiringInterface delegate) {
		this.delegate = delegate;
	}

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
		return new Injector(_WiringDoer.getContext(contextName));
	}

	@Override
	public Injector setLogger(LogBindingInterface logger) {
		delegate.setLogger(logger);
		return this;
	}

	@Override
	public <T> Injector defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		delegate.defineConstruction(clz, construction);
		return this;
	}

	@Override
	public <T> Injector defineStart(Class<? super T> clz, Consumer<? super T> start) {
		delegate.defineStart(clz, start);
		return this;
	}

	@Override
	public <T> Injector defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		delegate.defineStop(clz, stop);
		return this;
	}

	@Override
	public <T> Injector defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop) {
		delegate.defineStartStop(clz, start, stop);
		return this;
	}

	@Override
	public <T extends Dependent> Injector makeBeans(Class<T> classDependent) {
		delegate.makeBeans(classDependent);
		return this;
	}

	@Override
	public Injector start() {
		delegate.start();
		return this;
	}

	@Override
	public Injector stop() {
		delegate.stop();
		return this;
	}

	@Override
	public <T> T getBean(Class<T> clz) {
		return delegate.getBean(clz);
	}

	@Override
	public void print() {
		delegate.print();
	}

	@Override
	public void print(PrintStream out) {
		delegate.print(out);
	}

	/**
	 * Frees all internally used memory af all injectors. Use this carefully! It can
	 * be used to free memory after injection is done with
	 * {@link #makeBeans(Class)} and optionally the lifecycle start
	 * {@link #start()} was used. In this case the lifecycle support
	 * {@link Injector#stop()} does not work anymore as all context information was
	 * deleted.
	 */
	public static void resetAll() {
		_WiringDoer.resetAll();
	}

}
