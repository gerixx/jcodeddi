package coded.dependency.injection.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.LogBindingInterface;
import coded.dependency.injection.Wiring;

public class _WiringHelper {

	private final static Map<String, _WiringHelper> wiringContextMap = new HashMap<>();

	public final Map<Dependent, List<Dependency<?>>> dependencies = new HashMap<>();

	private static ThreadLocal<String> context = new ThreadLocal<>();

	private String contextName;

	private LogBindingInterface logger;

	public static _WiringHelper setContext(String ctx) {
		_WiringHelper.context.set(ctx);
		return getContext(ctx);
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

	public void setLogger(LogBindingInterface logger) {
		this.logger = logger;

	}

	// TODO use suppliers
	public void loginfo(Class<?> clz, Supplier<String> msg) {
		if (logger != null) {
			logger.info(clz, contextName, msg);
		}
	}

	public void logerror(Class<Wiring> clz, Supplier<String> msg) {
		if (logger != null) {
			logger.error(clz, contextName, msg);
		}
	}

	public void logerror(Class<?> clz, Supplier<String> msg, Exception e) {
		if (logger != null) {
			logger.error(clz, contextName, msg, e);
		}
	}

	public static String getPrintName(Object object) {
		return object.getClass()
			.getSimpleName() + " ("
				+ object.getClass()
					.getName()
				+ ")";
	}

	public static String getPrintName(String name, Object object) {
		final String printName;
		String fullClassName = object.getClass()
			.getName();
		if (!name.equals(fullClassName)) {
			printName = name + " (" + fullClassName + ")";
		} else {
			printName = object.getClass()
				.getSimpleName() + " (" + fullClassName + ")";
		}
		return printName;
	}

}
