package coded.dependency.injection;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import coded.dependency.ijection.internal._WiringHelper;

public class Wiring {

	private final static Map<String, Wiring> wiringContextMap = new HashMap<>();
	private final Map<String, Object> objectMap = new HashMap<>();
	private final Map<String, Supplier<?>> objectConstructionMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStopMap = new HashMap<>();
	private final Map<String, Consumer<?>> objectStartMap = new HashMap<>();
	private final List<String> objectConstructionSequenceList = new ArrayList<>();
	private final List<String> connectAllList = new ArrayList<>();
	private final String contextName;
	private State state = State.WIRING_IN_PROGRESS;

	private Wiring(String name) {
		this.contextName = name;
	}

	/**
	 * Creates a named injector. "Singletons" refer to single instances within an
	 * injector. With multiple injectors everyone would hold its own instance of a
	 * specific type.
	 * 
	 * @return the injector
	 */
	public static Wiring getContext(String contextName) {
		wiringContextMap.putIfAbsent(contextName, new Wiring(contextName));
		return wiringContextMap.get(contextName);
	}

	public Wiring setMaxWorkerThreads(int t) {
		return this;
	}

	/**
	 * Optional, otherwise default constructor is used.
	 * 
	 * @param clz
	 * @param construction
	 * @return the injector
	 */
	public <T> Wiring defineConstruction(Class<? super T> clz, Supplier<? super T> construction) {
		return define(clz, construction, null, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStart(Class<? super T> clz, Consumer<? super T> start) {
		return define(clz, null, start, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		return define(clz, null, null, stop);
	}

	/**
	 * Optional
	 */
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
	 * Creates dependency objects and wires them up recursively. Defined
	 * construction supplier or no-argument constructors are invoked to create
	 * objects if not created yet. Objects are treated as 'singletons' within the
	 * Wiring context. Multiple connects of classes are ignored.
	 * 
	 * @param <T>
	 * @param classDependent class to begin with recursive wiring
	 * @return injector
	 * @throws Exception
	 */
	public <T extends Dependent> Wiring connectAll(Class<T> classDependent) throws Exception {
		_WiringHelper.setContext(contextName);
		try {
			getOrCreateObject(classDependent, true);
			connectAllList.add(classDependent.getName());
		} finally {
			_WiringHelper.setContext(null);
		}
		return this;
	}

	public Wiring await() {
		// TODO
		return this;
	}

	public Wiring start() {
		this.state = State.START_IN_PROGRESS;
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		connectAllList.forEach(name -> {
			Dependent object = (Dependent) objectMap.get(name);
			startDependencies(helper, name, object);
		});
		this.state = State.START_FINISHED;
		return this;
	}

	private void startDependencies(_WiringHelper helper, String name, Object object) {
		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = helper.getDependencies((Dependent) object);
			dependencies.forEach(dep -> {
				startDependencies(helper, dep.get()
					.getClass()
					.getName(), dep);
			});
		}
		Consumer<?> consumer = objectStartMap.get(name);
		if (consumer != null) {
			consumer.accept(this.getTypedObject(name));
		}
	}

	public Wiring stop() {
		this.state = State.STOP_IN_PROGRESS;
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		connectAllList.forEach(name -> {
			Dependent object = (Dependent) objectMap.get(name);
			stopDependencies(helper, name, object);
		});
		this.state = State.STOP_FINISHED;
		return this;
	}

	private void stopDependencies(_WiringHelper helper, String name, Object object) {
		if (object instanceof Dependent) {
			List<Dependency<?>> dependencies = helper.getDependencies((Dependent) object);
			dependencies.forEach(dep -> {
				stopDependencies(helper, dep.get()
					.getClass()
					.getName(), dep);
			});
		}
		Consumer<?> consumer = objectStopMap.get(name);
		if (consumer != null) {
			consumer.accept(this.getTypedObject(name));
		}
	}

	/**
	 * Returns singleton instance of given class or null if not existing.
	 * 
	 * @param <T> dedicated type
	 * @param clz class
	 * @return singleton or null
	 */
	public <T> T get(Class<T> clz) {
		return get(clz.getName());
	}

	private <T> T get(String name) {
		await();
		return getTypedObject(name);
	}

	public State getState() {
		return state;
	}

	public void print() {
		print(System.out);
	}

	// A -> B
	private String indent = "";
	private Set<String> traversedObjects = new HashSet<>();

	public void print(PrintStream out) {
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		connectAllList.forEach(name -> {
			Object object = objectMap.get(name);
			if (object instanceof Dependent) {
				String depName = getPrintName(name, object);
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
				String targetNameToPrint = getPrintName(targetName, target);
				out.println(targetNameToPrint);
				if (!traversedObjects.contains(targetName)) {
					traversedObjects.add(targetName);
					if (Dependent.class.isAssignableFrom(dep.getTargetClass())) {
						printDependencies(out, helper, (Dependent) dep.get());
					}
				}
			}
		});
		indent = indent.substring(0, indent.length() - 2);
	}

	private String getPrintName(String name, Object object) {
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

	@SuppressWarnings("unchecked")
	private <T> T getTypedObject(String name) {
		return (T) objectMap.get(name);
	}

	// TODO move to helper
	public Object getOrCreateObject(Class<?> clz, boolean isExplicitWiring) throws Exception {
		String name = clz.getName();
		if (!objectMap.containsKey(name)) {
			final Object newObject;
			if (objectConstructionMap.containsKey(name)) {
				newObject = objectConstructionMap.get(name)
					.get();
				if (!newObject.getClass()
					.getName()
					.equals(name)) {
					// name is an interface, map it additionally to the new object
					String nameImpl = newObject.getClass()
						.getName();
					objectMap.put(nameImpl, newObject);
					objectConstructionSequenceList.add(nameImpl);
				}
			} else {
				newObject = clz.getDeclaredConstructor()
					.newInstance();
			}
			objectMap.put(name, newObject);
			objectConstructionSequenceList.add(name);
		}
		return objectMap.get(name);

	}

	/**
	 * Clears all contexts.
	 */
	public static void resetAll() {
		wiringContextMap.clear();
		_WiringHelper.restAll();
	}

}
