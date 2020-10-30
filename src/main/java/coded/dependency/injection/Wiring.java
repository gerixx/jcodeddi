package coded.dependency.injection;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.exception.ConnectAllException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.internal._WiringHelper;

public class Wiring implements WiringInterface {

	private final static Map<String, Wiring> wiringContextMap = new HashMap<>();
	private final Map<String, Object> objectMap = new HashMap<>();
	private final Map<String, Supplier<?>> objectConstructionMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStopMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStartMap = new HashMap<>();
	private final Set<String> objectCreationPending = new HashSet<>();
	private final List<String> connectAllList = new ArrayList<>();
	private final String contextName;

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

	private Wiring(String name) {
		this.contextName = name;
		_WiringHelper.getContext(contextName)
			.setLogger(new LogBindingAdapter(new PrintWriter(System.out, true)));
	}

	/**
	 * Creates a named injector. "Singletons" refer to single instances within an
	 * injector. With multiple injectors everyone would hold its own instance of a
	 * specific type.
	 * 
	 * @return the injector
	 */
	public static WiringInterface getContext(String contextName) {
		if (!wiringContextMap.containsKey(contextName)) {
			wiringContextMap.put(contextName, new Wiring(contextName));
		}
		return wiringContextMap.get(contextName);
	}

	/*
	 * Set your own log target by implementing {@link LogBindingInterface}. Default
	 * log target is System.out. Set null to disable logs.
	 */
	@Override
	public Wiring setLogger(LogBindingInterface logger) {
		_WiringHelper.getContext(contextName)
			.setLogger(logger);
		return this;
	}

	/**
	 * Optional, otherwise default constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	@Override
	public <T> Wiring defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		return define(clz, construction, null, null);
	}

	/**
	 * Optional
	 */
	@Override
	public <T> Wiring defineStart(Class<? super T> clz, Consumer<? super T> start) {
		return define(clz, null, start, null);
	}

	/**
	 * Optional
	 */
	@Override
	public <T> Wiring defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		return define(clz, null, null, stop);
	}

	/**
	 * Optional
	 */
	@Override
	public <T> Wiring defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop) {
		return define(clz, null, start, stop);
	}

	private <T> Wiring define(Class<? super T> clz, Supplier<? super T> construction, Consumer<? super T> start,
			Consumer<? super T> stop) {
		String name = clz.getName();
		if (construction != null) {
			objectConstructionMap.put(name, construction);
		}
		if (start != null) {
			objectStartMap.put(name, start);
		}
		if (stop != null) {
			objectStopMap.put(name, stop);
		}
		return this;
	}

	/**
	 * Creates bean with its dependency objects and wires them up recursively.
	 * Defined construction suppliers or no-argument constructors are invoked to
	 * create beans if not created yet. Beans are treated as 'singletons' within the
	 * Wiring context. Multiple connects of classes are ignored.
	 * 
	 * @param <T>
	 * @param classDependent class of bean to begin with recursive construction and
	 *                       injection
	 * @return injector
	 * @throws Exception
	 */
	@Override
	public <T extends Dependent> Wiring connectAll(Class<T> classDependent) {
		_WiringHelper helper = _WiringHelper.setContext(contextName);
		helper.loginfo(Wiring.class,
				() -> "Connect all of dependent " + _WiringHelper.getPrintNameOfClass(classDependent) + " ...");
		StopWatch start = StopWatch.start();
		try {
			getOrCreateObject(classDependent);
			connectAllList.add(classDependent.getName());
		} catch (Exception e) {
			if (_WiringHelper.isCauseKnownRuntimeException(e)) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new ConnectAllException(e);
			}
		} finally {
			_WiringHelper.resetContext();
		}
		helper.loginfo(Wiring.class, () -> "Connect all finished in " + start.stop() + "ms.");
		return this;
	}

	/**
	 * Runs for all beans its start method if it was defined by
	 * {@link #defineStart(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	@Override
	public Wiring start() {
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		if (connectAllList.isEmpty()) {
			helper.logerror(Wiring.class, () -> "No class injection done yet, see .connectAll().");
		} else {
			helper.loginfo(Wiring.class, () -> "Start beans...");
			StopWatch start = StopWatch.start();
			connectAllList.forEach(name -> {
				Dependent object = (Dependent) objectMap.get(name);
				startDependencies(helper, name, object);
			});
			helper.loginfo(Wiring.class, () -> "Start beans finished in " + start.stop() + "ms.");
		}
		return this;
	}

	private void startDependencies(_WiringHelper helper, String name, Object object) {
		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = helper.getDependencies((Dependent) object);
			dependencies.forEach(dep -> {
				startDependencies(helper, dep.get()
					.getClass()
					.getName(), dep.get());
			});
		}
		Consumer<?> consumer = objectStartMap.get(name);
		StopWatch start = StopWatch.start();
		if (consumer != null) {
			consumer.accept(this.getTypedObject(name));
			helper.loginfo(Wiring.class, () -> "Started " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Consumer in " + start.stop() + "ms.");
		} else if (objectMap.get(name) instanceof Lifecycle) {
			((Lifecycle) objectMap.get(name)).start();
			helper.loginfo(Wiring.class, () -> "Started " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
		}
	}

	/**
	 * Runs for all beans its stop method if it was defined by
	 * {@link #defineStop(Class, Consumer)} or by the implementation of the
	 * {@link Lifecycle} interface.
	 */
	@Override
	public Wiring stop() {
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		helper.loginfo(Wiring.class, () -> "Stop beans...");
		StopWatch start = StopWatch.start();
		connectAllList.forEach(name -> {
			Dependent object = (Dependent) objectMap.get(name);
			stopDependencies(helper, name, object);
		});
		helper.loginfo(Wiring.class, () -> "Stop beans finished in " + start.stop() + "ms.");
		return this;
	}

	private void stopDependencies(_WiringHelper helper, String name, Object object) {
		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = helper.getDependencies((Dependent) object);
			dependencies.forEach(dep -> {
				stopDependencies(helper, dep.get()
					.getClass()
					.getName(), dep.get());
			});
		}
		StopWatch start = StopWatch.start();
		Consumer<?> consumer = objectStopMap.get(name);
		if (consumer != null) {
			consumer.accept(this.getTypedObject(name));
			helper.loginfo(Wiring.class, () -> "Stopped " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Consumer in " + start.stop() + "ms.");
		} else if (objectMap.get(name) instanceof Lifecycle) {
			((Lifecycle) objectMap.get(name)).stop();
			helper.loginfo(Wiring.class, () -> "Stopped " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
		}
	}

	/**
	 * Retrieves bean by given class of the current context or null if it was not
	 * found.
	 * 
	 * @param <T> dedicated type
	 * @param clz class
	 * @return bean or null
	 */
	@Override
	public <T> T get(Class<T> clz) {
		return get(clz.getName());
	}

	private <T> T get(String name) {
		return getTypedObject(name);
	}

	@Override
	public void print() {
		print(System.out);
	}

	// A -> B
	private String indent = "";
	private Set<String> traversedObjects = new HashSet<>();

	@Override
	public void print(PrintStream out) {
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		connectAllList.forEach(name -> {
			Object object = objectMap.get(name);
			if (object instanceof Dependent) {
				String depName = _WiringHelper.getPrintName(name, object);
				out.print(indent);
				out.println(depName);
				traversedObjects.add(name);
				printDependencies(out, helper, (Dependent) object);
			}
		});
	}

	private void printDependencies(PrintStream out, _WiringHelper helper, Dependent object) {
		List<Dependency<?>> dependencies = helper.getDependencies(object);
		indent += "  ";
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
				String targetNameToPrint = _WiringHelper.getPrintName(targetName, target);
				out.println(targetNameToPrint);
				if (!traversedObjects.contains(targetName)) {
					traversedObjects.add(targetName);
					if (Dependent.class.isAssignableFrom(dep.get()
						.getClass())) {
						printDependencies(out, helper, (Dependent) dep.get());
					}
				}
			}
		});
		indent = indent.substring(0, indent.length() - 2);
	}

	@SuppressWarnings("unchecked")
	private <T> T getTypedObject(String name) {
		return (T) objectMap.get(name);
	}

	// TODO move to helper
	public Object getOrCreateObject(Class<?> clz) throws Exception {
		String name = clz.getName();
		if (!objectMap.containsKey(name)) {
			objectCreationPending.add(name);
			_WiringHelper helper = _WiringHelper.getContext(contextName);
			StopWatch start = StopWatch.start();
			final Object newObject;
			if (objectConstructionMap.containsKey(name)) {
				newObject = objectConstructionMap.get(name)
					.get();
				helper.loginfo(Wiring.class, () -> "Created " + _WiringHelper.getPrintName(newObject)
						+ " using Supplier in " + start.stop() + "ms.");
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
							+ "', use Wiring#defineConstruction(...).");
				}
				newObject = clz.getDeclaredConstructor()
					.newInstance();
				helper.loginfo(Wiring.class, () -> "Created " + _WiringHelper.getPrintName(newObject)
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

	public <T> Object getOrCreateObject(Dependency<T> dependency, Class<?> targetClass) throws Exception {
		if (objectCreationPending.contains(targetClass.getName())) {
			// TODO track dependent and log it
			throw new CyclicDependencyException(
					"Cyclic dependency to " + _WiringHelper.getPrintNameOfClass(targetClass));
		}
		return getOrCreateObject(targetClass);
	}

	/**
	 * Clears all contexts.
	 */
	public static void resetAll() {
		wiringContextMap.clear();
		_WiringHelper.restAll();
	}

}
