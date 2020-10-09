package coded.dependency.injection;

/**
 * If implemented, {@link Wiring#start()} and {@link Wiring#stop()} will invoke
 * this accordingly. It is ignored if a start and/or a stop consumer are
 * defined, see {@link Wiring#defineStart(Class, java.util.function.Consumer)}
 * and {@link Wiring#defineStop(Class, java.util.function.Consumer)}.
 *
 */
public interface Lifecycle {

	public void start();

	public void stop();

}
