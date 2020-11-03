package coded.dependency.injection;

import coded.dependency.injection.internal._WiringDoer;

/**
 * If implemented, {@link _WiringDoer#start()} and {@link _WiringDoer#stop()} will invoke
 * this accordingly. It is ignored if a start and/or a stop consumer are
 * defined, see {@link _WiringDoer#defineStart(Class, java.util.function.Consumer)}
 * and {@link _WiringDoer#defineStop(Class, java.util.function.Consumer)}.
 *
 */
public interface Lifecycle {

	public void start();

	public void stop();

}
