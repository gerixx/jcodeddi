package coded.dependency.injection.internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.Injector;
import coded.dependency.injection.Lifecycle;
import coded.dependency.injection.LogBindingAdapter;
import coded.dependency.injection.LogBindingInterface;
import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.ContextMismatchException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.exception.DependencyCreationException;
import coded.dependency.injection.exception.MakeBeansException;

public class _WiringHelper implements Injector {

	private final static Map<String, _WiringHelper> wiringContextMap = new HashMap<>();
	private final static ThreadLocal<String> threadContext = new ThreadLocal<>();

	private final Map<String, Object> objectMap = new HashMap<>();
	private final Map<String, Supplier<?>> objectConstructionMap = new HashMap<>();
	private final Set<String> objectStartedSet = new HashSet<>();
	private final Set<String> objectStoppedSet = new HashSet<>();
	private final Set<String> objectCreationPending = new HashSet<>();
	private final List<String> makeBeansList = new ArrayList<>();
	private final String contextName;
	private final Map<Dependent, List<Dependency<?>>> dependencies = new HashMap<>();

	private Optional<LogBindingInterface> logger = Optional.empty();

	public static class StopWatch {
		private Instant start;

		StopWatch() {
			start = Instant.now();
		}

		static StopWatch start() {
			return new StopWatch();
		}

		long stop() {
			return Duration.between(start, Instant.now())
				.toMillis();
		}
	}

	private _WiringHelper(String name) {
		this.contextName = name;
		setLogger(new LogBindingAdapter(new PrintWriter(System.out, true)));
	}

	/**
	 * Creates a named injector. "Singletons" refer to single instances within an
	 * injector. With multiple injectors everyone would hold its own instance of a
	 * specific type.
	 * 
	 * @return the injector
	 */
	public static Injector getOrCreateContext(String contextName) {
		if (contextName == null) {
			throw new IllegalArgumentException("contextName must not be NULL");
		}
		_WiringHelper wiring = getContext(contextName);
		if (wiring == null) {
			synchronized (wiringContextMap) {
				return wiringContextMap.computeIfAbsent(contextName, k -> new _WiringHelper(k));
			}
		}
		return wiring;
	}

	public static String[] getContextNames() {
		return wiringContextMap.keySet()
			.toArray(String[]::new);
	}

	@Override
	public String getName() {
		return contextName;
	}

	@Override
	public Injector setLogger(LogBindingInterface logger) {
		this.logger = Optional.ofNullable(logger);
		return this;
	}

	@Override
	public <T> Injector defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		return define(clz, construction);
	}

	private <T> _WiringHelper define(Class<? super T> clz, Supplier<? super T> construction) {
		String name = clz.getName();
		if (construction != null) {
			objectConstructionMap.put(name, construction);
		}
		return this;
	}

	@Override
	public <T extends Dependent> Injector makeBeans(Class<T> classDependent) {
		loginfo(_WiringHelper.class, () -> "Make beans for dependent " + getPrintNameOfClass(classDependent) + " ...");
		StopWatch start = StopWatch.start();
		try {
			setThreadContext(contextName);
			getOrCreateObjectImpl(classDependent);
			makeBeansList.add(classDependent.getName());
		} catch (ContextMismatchException | BeanOutOfContextCreationException | CyclicDependencyException
				| ConstructionMissingException | DependencyCreationException e) {
			throw e;
		} catch (Exception e) {
			if (isCauseKnownRuntimeException(e)) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new MakeBeansException(e);
			}
		} finally {
			resetThreadContext();
		}
		loginfo(_WiringHelper.class, () -> "Make beans finished in " + start.stop() + "ms.");
		return this;
	}

	@Override
	public Injector start() {
		if (makeBeansList.isEmpty()) {
			logerror(_WiringHelper.class, () -> "No class injection done yet, see .makeBeans(...).");
		} else {
			loginfo(_WiringHelper.class, () -> "Start beans...");
			StopWatch start = StopWatch.start();
			makeBeansList.forEach(name -> {
				Dependent object = (Dependent) objectMap.get(name);
				startDependencies(name, object);
			});
			loginfo(_WiringHelper.class, () -> "Start beans finished in " + start.stop() + "ms.");
		}
		return this;
	}

	private void startDependencies(String name, Object object) {
		if (objectStartedSet.contains(name)) {
			return;
		}

		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = getDependencies((Dependent) object);
			if (dependencies != null) {
				dependencies.forEach(dep -> {
					startDependencies(dep.get()
						.getClass()
						.getName(), dep.get());
				});
			}
		}
		if (objectMap.get(name) instanceof Lifecycle) {
			StopWatch start = StopWatch.start();
			((Lifecycle) objectMap.get(name)).start();
			objectStartedSet.add(name);
			loginfo(_WiringHelper.class, () -> "Started " + getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
		}
	}

	@Override
	public Injector stop() {
		loginfo(_WiringHelper.class, () -> "Stop beans...");
		StopWatch start = StopWatch.start();
		makeBeansList.forEach(name -> {
			Dependent object = (Dependent) objectMap.get(name);
			stopDependencies(name, object);
		});
		loginfo(_WiringHelper.class, () -> "Stop beans finished in " + start.stop() + "ms.");
		return this;
	}

	private void stopDependencies(String name, Object object) {
		if (objectStoppedSet.contains(name)) {
			return;
		}

		if (objectMap.get(name) instanceof Lifecycle) {
			StopWatch start = StopWatch.start();
			((Lifecycle) objectMap.get(name)).stop();
			objectStoppedSet.add(name);
			loginfo(_WiringHelper.class, () -> "Stopped " + getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
		}
		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = getDependencies((Dependent) object);
			if (dependencies != null) {
				dependencies.forEach(dep -> {
					stopDependencies(dep.get()
						.getClass()
						.getName(), dep.get());
				});
			}
		}
	}

	@Override
	public <T> T getBean(Class<T> clz) {
		return get(clz.getName());
	}

	private <T> T get(String name) {
		return getTypedObject(name);
	}

	@Override
	public Injector print() {
		print(System.out);
		return this;
	}

	// A -> B
	private String indent = "";
	private Set<String> traversedObjects = new HashSet<>();

	@Override
	public Injector print(PrintStream out) {
		makeBeansList.forEach(name -> {
			Object object = objectMap.get(name);
			if (object instanceof Dependent) {
				String depName = getPrintName(name, object);
				out.print(indent);
				out.println(depName);
				traversedObjects.add(name);
				printDependencies(out, (Dependent) object);
			}
			traversedObjects.clear();
		});
		return this;
	}

	private void printDependencies(PrintStream out, Dependent object) {
		List<Dependency<?>> dependencies = getDependencies(object);
		indent += "  ";
		if (dependencies != null) {
			dependencies.forEach(dep -> {
				out.print(indent);
				out.print("-> ");
				Object target = dep.get();
				if (target == null) {
					out.println("UNRESOLVED dependency to: " + dep.getTargetClass()
						.getName());
				} else {
					String targetName = target.getClass()
						.getName();
					String targetNameToPrint = getPrintName(targetName, target);
					out.println(targetNameToPrint);
					if (!traversedObjects.contains(targetName)) {
						traversedObjects.add(targetName);
						if (Dependent.class.isAssignableFrom(dep.get()
							.getClass())) {
							printDependencies(out, (Dependent) dep.get());
						}
					}
				}
			});
		}
		indent = indent.substring(0, indent.length() - 2);
	}

	@SuppressWarnings("unchecked")
	private <T> T getTypedObject(String name) {
		return (T) objectMap.get(name);
	}

	private Object getOrCreateObjectImpl(Class<?> clz) throws Exception {
		String name = clz.getName();
		if (!objectMap.containsKey(name)) {
			objectCreationPending.add(name);
			StopWatch start = StopWatch.start();
			final Object newObject;
			if (objectConstructionMap.containsKey(name)) {
				newObject = objectConstructionMap.get(name)
					.get();
				loginfo(_WiringHelper.class,
						() -> "Created " + getPrintName(newObject) + " using Supplier in " + start.stop() + "ms.");
				if (!newObject.getClass()
					.getName()
					.equals(name)) {
					String nameImpl = newObject.getClass()
						.getName();
					objectMap.put(nameImpl, newObject);
				}
			} else {
				if (clz.isInterface()) {
					throw new ConstructionMissingException("Construction needed for interface '" + clz.getName()
							+ "', use Injector#defineConstruction(...).");
				}
				newObject = clz.getDeclaredConstructor()
					.newInstance();
				loginfo(_WiringHelper.class, () -> "Created " + getPrintName(newObject)
						+ " using default consctructor in " + start.stop() + "ms.");
			}
			objectMap.put(name, newObject);
		}
		handleRecursiveDependencies(name);
		return objectMap.get(name);
	}

	private void handleRecursiveDependencies(String name) {
		objectCreationPending.remove(name);
	}

	private <T> Object getOrCreateObject(Class<?> targetClass) throws Exception {
		if (objectCreationPending.contains(targetClass.getName())) {
			// TODO track dependent and log it
			throw new CyclicDependencyException("Cyclic dependency to " + getPrintNameOfClass(targetClass));
		}
		return getOrCreateObjectImpl(targetClass);
	}

	/**
	 * Clears all injectors.
	 */
	public static void removeAll() {
		synchronized (wiringContextMap) {
			wiringContextMap.clear();
			threadContext.set(null);
		}
	}

	@Override
	public Injector remove() {
		synchronized (wiringContextMap) {
			wiringContextMap.remove(contextName);
			return this;
		}
	}

	public static _WiringHelper setThreadContext(String ctx) {
		String contextName = threadContext.get();
		if (contextName != null && !ctx.equals(contextName)) {
			throw new ContextMismatchException(String.format(
					"Initialization of context '%s' is not finisihed. New context '%s' cannot be created.", contextName,
					ctx));
		}
		_WiringHelper context = getContext(ctx);
		if (context == null) {
			throw new IllegalStateException(String.format("Context '%s' does not exist.", ctx));
		}
		threadContext.set(ctx);
		return context;
	}

	public static _WiringHelper getContext(String contextName) {
		return wiringContextMap.get(contextName);
	}

	public static _WiringHelper getThreadContext() throws _NoContextDefinedException {
		String contextName = threadContext.get();
		if (contextName == null) {
			throw new _NoContextDefinedException();
		}
		return getContext(contextName);
	}

	public static void resetThreadContext() {
		threadContext.set(null);
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

	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> targetClass) throws Exception {
		return (T) getOrCreateObject(targetClass);
	}

	public void loginfo(Class<?> clz, Supplier<String> msg) {
		if (logger.isPresent()) {
			logger.get()
				.info(contextName, new Throwable().getStackTrace(), msg);
		}
	}

	public void logerror(Class<?> clz, Supplier<String> msg) {
		if (logger.isPresent()) {
			logger.get()
				.error(contextName, new Throwable().getStackTrace(), msg);
		}
	}

	public void logerror(Class<?> clz, Supplier<String> msg, Exception e) {
		if (logger.isPresent()) {
			logger.get()
				.error(contextName, new Throwable().getStackTrace(), msg, e);
		}
	}

	private static String getPrintName(Object object) {
		return getPrintNameOfClass(object.getClass());
	}

	private static String getPrintNameOfClass(Class<?> clz) {
		return clz.getSimpleName() + " (" + clz.getName() + ")";
	}

	private static String getPrintName(String name, Object object) {
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
				&& (cause instanceof ContextMismatchException || cause instanceof BeanOutOfContextCreationException
						|| cause instanceof CyclicDependencyException || cause instanceof ConstructionMissingException
						|| cause instanceof DependencyCreationException);
	}

}
