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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.Lifecycle;
import coded.dependency.injection.LogBindingAdapter;
import coded.dependency.injection.LogBindingInterface;
import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConnectAllException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.exception.DependencyCreationException;

public class _WiringDoer implements _WiringInterface {

	private final static Map<String, _WiringDoer> wiringContextMap = new HashMap<>();
	private final Map<String, Object> objectMap = new HashMap<>();
	private final Map<String, Supplier<?>> objectConstructionMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStopMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStartMap = new HashMap<>();
	private final Set<String> objectCreationPending = new HashSet<>();
	private final List<String> makeBeansList = new ArrayList<>();
	private final String contextName;
	private final _WiringHelper helper;

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

	private _WiringDoer(String name) {
		this.contextName = name;
		this.helper = _WiringHelper.getContext(contextName);
		helper.setLogger(new LogBindingAdapter(new PrintWriter(System.out, true)));
	}

	/**
	 * Creates a named injector. "Singletons" refer to single instances within an
	 * injector. With multiple injectors everyone would hold its own instance of a
	 * specific type.
	 * 
	 * @return the injector
	 */
	public static _WiringInterface getContext(String contextName) {
		_WiringDoer wiring = wiringContextMap.get(contextName);
		if (wiring == null) {
			synchronized (wiringContextMap) {
				return wiringContextMap.computeIfAbsent(contextName, k -> new _WiringDoer(k));
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
	public _WiringInterface setLogger(LogBindingInterface logger) {
		helper.setLogger(logger);
		return this;
	}

	@Override
	public <T> _WiringInterface defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		return define(clz, construction, null, null);
	}

	@Override
	public <T> _WiringInterface defineStart(Class<? super T> clz, Consumer<? super T> start) {
		return define(clz, null, start, null);
	}

	@Override
	public <T> _WiringInterface defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		return define(clz, null, null, stop);
	}

	@Override
	public <T> _WiringInterface defineStartStop(Class<? super T> clz, Consumer<? super T> start,
			Consumer<? super T> stop) {
		return define(clz, null, start, stop);
	}

	private <T> _WiringDoer define(Class<? super T> clz, Supplier<? super T> construction, Consumer<? super T> start,
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

	@Override
	public <T extends Dependent> _WiringInterface makeBeans(Class<T> classDependent) {
		_WiringHelper helper = _WiringHelper.setContext(contextName);
		helper.loginfo(_WiringDoer.class,
				() -> "Make beans for dependent " + _WiringHelper.getPrintNameOfClass(classDependent) + " ...");
		StopWatch start = StopWatch.start();
		try {
			getOrCreateObject(classDependent);
			makeBeansList.add(classDependent.getName());
		} catch (BeanOutOfContextCreationException | CyclicDependencyException | ConstructionMissingException
				| DependencyCreationException e) {
			throw e;
		} catch (Exception e) {
			if (_WiringHelper.isCauseKnownRuntimeException(e)) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new ConnectAllException(e);
			}
		} finally {
			_WiringHelper.resetContext();
		}
		helper.loginfo(_WiringDoer.class, () -> "Make beans finished in " + start.stop() + "ms.");
		return this;
	}

	@Override
	public _WiringInterface start() {
		if (makeBeansList.isEmpty()) {
			helper.logerror(_WiringDoer.class, () -> "No class injection done yet, see .makeBeans(...).");
		} else {
			helper.loginfo(_WiringDoer.class, () -> "Start beans...");
			StopWatch start = StopWatch.start();
			makeBeansList.forEach(name -> {
				Dependent object = (Dependent) objectMap.get(name);
				startDependencies(helper, name, object);
			});
			helper.loginfo(_WiringDoer.class, () -> "Start beans finished in " + start.stop() + "ms.");
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
			helper.loginfo(_WiringDoer.class, () -> "Started " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Consumer in " + start.stop() + "ms.");
		} else if (objectMap.get(name) instanceof Lifecycle) {
			((Lifecycle) objectMap.get(name)).start();
			helper.loginfo(_WiringDoer.class, () -> "Started " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
		}
	}

	@Override
	public _WiringInterface stop() {
		helper.loginfo(_WiringDoer.class, () -> "Stop beans...");
		StopWatch start = StopWatch.start();
		makeBeansList.forEach(name -> {
			Dependent object = (Dependent) objectMap.get(name);
			stopDependencies(helper, name, object);
		});
		helper.loginfo(_WiringDoer.class, () -> "Stop beans finished in " + start.stop() + "ms.");
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
			helper.loginfo(_WiringDoer.class, () -> "Stopped " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Consumer in " + start.stop() + "ms.");
		} else if (objectMap.get(name) instanceof Lifecycle) {
			((Lifecycle) objectMap.get(name)).stop();
			helper.loginfo(_WiringDoer.class, () -> "Stopped " + _WiringHelper.getPrintName(this.getTypedObject(name))
					+ " using Lifecycle in " + start.stop() + "ms.");
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
	public _WiringInterface print() {
		print(System.out);
		return this;
	}

	// A -> B
	private String indent = "";
	private Set<String> traversedObjects = new HashSet<>();

	@Override
	public _WiringInterface print(PrintStream out) {
		makeBeansList.forEach(name -> {
			Object object = objectMap.get(name);
			if (object instanceof Dependent) {
				String depName = _WiringHelper.getPrintName(name, object);
				out.print(indent);
				out.println(depName);
				traversedObjects.add(name);
				printDependencies(out, helper, (Dependent) object);
			}
			traversedObjects.clear();
		});
		return this;
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

	public Object getOrCreateObject(Class<?> clz) throws Exception {
		String name = clz.getName();
		if (!objectMap.containsKey(name)) {
			objectCreationPending.add(name);
			StopWatch start = StopWatch.start();
			final Object newObject;
			if (objectConstructionMap.containsKey(name)) {
				newObject = objectConstructionMap.get(name)
					.get();
				helper.loginfo(_WiringDoer.class, () -> "Created " + _WiringHelper.getPrintName(newObject)
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
							+ "', use Injector#defineConstruction(...).");
				}
				newObject = clz.getDeclaredConstructor()
					.newInstance();
				helper.loginfo(_WiringDoer.class, () -> "Created " + _WiringHelper.getPrintName(newObject)
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
	 * Clears all injectors.
	 */
	public static void removeAll() {
		synchronized (wiringContextMap) {
			wiringContextMap.clear();
			_WiringHelper.restAll();
		}
	}

	@Override
	public _WiringInterface remove() {
		synchronized (wiringContextMap) {
			wiringContextMap.remove(contextName);
			helper.remove();
			return this;
		}
	}

}
