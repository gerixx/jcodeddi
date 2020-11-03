package coded.dependency.injection;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.internal._WiringDoer;
import coded.dependency.injection.internal._WiringInterface;

public class Wiring implements _WiringInterface {

	private _WiringInterface delegate;

	private Wiring(_WiringInterface delegate) {
		this.delegate = delegate;
	}

	public static Wiring getContext(String contextName) {
		return new Wiring(_WiringDoer.getContext(contextName));
	}

	@Override
	public Wiring setLogger(LogBindingInterface logger) {
		delegate.setLogger(logger);
		return this;
	}

	@Override
	public <T> Wiring defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		delegate.defineConstruction(clz, construction);
		return this;
	}

	@Override
	public <T> Wiring defineStart(Class<? super T> clz, Consumer<? super T> start) {
		delegate.defineStart(clz, start);
		return this;
	}

	@Override
	public <T> Wiring defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		delegate.defineStop(clz, stop);
		return this;
	}

	@Override
	public <T> Wiring defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop) {
		delegate.defineStartStop(clz, start, stop);
		return this;
	}

	@Override
	public <T extends Dependent> Wiring connectAll(Class<T> classDependent) {
		delegate.connectAll(classDependent);
		return this;
	}

	@Override
	public Wiring start() {
		delegate.start();
		return this;
	}

	@Override
	public Wiring stop() {
		delegate.stop();
		return this;
	}

	@Override
	public <T> T get(Class<T> clz) {
		return delegate.get(clz);
	}

	@Override
	public void print() {
		delegate.print();
	}

	@Override
	public void print(PrintStream out) {
		delegate.print(out);
	}

	public static void resetAll() {
		_WiringDoer.resetAll();
	}

}
