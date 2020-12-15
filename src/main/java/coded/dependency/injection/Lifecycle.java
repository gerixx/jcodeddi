package coded.dependency.injection;

/**
 * If implemented, {@link Injector#start()} and {@link Injector#stop()} will
 * invoke this accordingly. It is ignored if a start and/or a stop consumer are
 * defined, see {@link Injector#defineStart(Class, java.util.function.Consumer)}
 * and {@link Injector#defineStop(Class, java.util.function.Consumer)}.
 *
 */
public interface Lifecycle {

	public void start();

	public void stop();

}
