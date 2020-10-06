package coded.dependency.ijection.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.Wiring;

public class _WiringHelper {

	private final static Map<String, _WiringHelper> wiringContextMap = new HashMap<>();

	public final Map<Dependent, List<Dependency<?>>> dependencies = new HashMap<>();

	private static ThreadLocal<String> context = new ThreadLocal<>();

	private String contextName;

	public static void setContext(String ctx) {
		_WiringHelper.context.set(ctx);
	}

	public static _WiringHelper getContext() {
		return getContext(context.get());
	}

	public static _WiringHelper getContext(String contextName) {
		wiringContextMap.putIfAbsent(contextName, new _WiringHelper(contextName));
		return wiringContextMap.get(contextName);
	}

	private _WiringHelper(String contextName) {
		this.contextName = contextName;
	}

	/**
	 * Internal use only! Adds a needed dependency.
	 * 
	 * @param d   the dependent, e.g., A if A depends on B
	 * @param dep the dependency proxy object providing the needed object, e.g., B
	 *            if A depends on B
	 */
	public void newDependency(Dependent d, Dependency<?> dep) {
		dependencies.putIfAbsent(d, new ArrayList<Dependency<?>>());
		dependencies.get(d)
			.add(dep);
	}

	public List<Dependency<?>> getDependencies(Dependent dependent) {
		return dependencies.get(dependent);
	}

	public static void restAll() {
		wiringContextMap.clear();
		context.set(null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<? extends T> targetClass) throws Exception {
		return (T) Wiring.getContext(contextName)
			.getOrCreateObject(targetClass, true);
	}

}
