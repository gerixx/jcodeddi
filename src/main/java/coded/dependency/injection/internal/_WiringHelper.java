package coded.dependency.injection.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.LogBindingInterface;
import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.exception.DependencyCreationException;

public class _WiringHelper {

	private final static Map<String, _WiringHelper> wiringContextMap = new HashMap<>();

	public final Map<Dependent, List<Dependency<?>>> dependencies = new HashMap<>();

	private static ThreadLocal<String> threadContext = new ThreadLocal<>();

	private String contextName;

	private LogBindingInterface logger;

	public static _WiringHelper setContext(String ctx) {
		_WiringHelper.threadContext.set(ctx);
		return getContext(ctx);
	}

	public static _WiringHelper getThreadContext() {
		String contextName = threadContext.get();
		if (contextName == null) {
			throw new BeanOutOfContextCreationException();
		}
		return getContext(contextName);
	}

	public static _WiringHelper getContext(String contextName) {
		if (contextName == null) {
			throw new IllegalArgumentException("contextName is null, not allowed");
		}
		if (!wiringContextMap.containsKey(contextName)) {
			wiringContextMap.put(contextName, new _WiringHelper(contextName));
		}
		return wiringContextMap.get(contextName);
	}

	public static void resetContext() {
		_WiringHelper.threadContext.set(null);
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
	public void addNewDependency(Dependent d, Dependency<?> dep) {
		if (!dependencies.containsKey(d)) {
			dependencies.put(d, new ArrayList<Dependency<?>>());
		}
		dependencies.get(d)
			.add(dep);
	}

	public List<Dependency<?>> getDependencies(Dependent dependent) {
		return dependencies.get(dependent);
	}

	public static void restAll() {
		wiringContextMap.clear();
		threadContext.set(null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(Dependency<T> dependency, Class<T> targetClass) throws Exception {
		return (T) ((_WiringDoer) _WiringDoer.getContext(contextName)).getOrCreateObject(dependency, targetClass);
	}

	public void setLogger(LogBindingInterface logger) {
		this.logger = logger;
	}

	public void loginfo(Class<?> clz, Supplier<String> msg) {
		if (logger != null) {
			logger.info(contextName, new Throwable().getStackTrace(), msg);
		}
	}

	public void logerror(Class<?> clz, Supplier<String> msg) {
		if (logger != null) {
			logger.error(contextName, new Throwable().getStackTrace(), msg);
		}
	}

	public void logerror(Class<?> clz, Supplier<String> msg, Exception e) {
		if (logger != null) {
			logger.error(contextName, new Throwable().getStackTrace(), msg, e);
		}
	}

	public static String getPrintName(Object object) {
		return getPrintNameOfClass(object.getClass());
	}

	public static String getPrintNameOfClass(Class<?> clz) {
		return clz.getSimpleName() + " (" + clz.getName() + ")";
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

	public static boolean isCauseKnownRuntimeException(Exception e) {
		Throwable cause = e.getCause();
		return cause != null && cause instanceof RuntimeException
				&& (cause instanceof BeanOutOfContextCreationException || cause instanceof CyclicDependencyException
						|| cause instanceof ConstructionMissingException
						|| cause instanceof DependencyCreationException);
	}

	public void remove() {
		wiringContextMap.remove(contextName);
	}
}
