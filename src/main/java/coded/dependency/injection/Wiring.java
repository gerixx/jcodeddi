package coded.dependency.injection;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private final String contextName;
	private State state = State.WIRING_IN_PROGRESS;
	private boolean isStrictConnect = true;

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
	 * If true {@link #connect(Class, Class)} throws an Exception if same dependency
	 * is connected twice. Default if false.
	 * 
	 * @param isStrictConnect
	 * @return the injector
	 */
	public Wiring setStrictConnect(boolean isStrictConnect) {
		this.isStrictConnect = isStrictConnect;
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
		return define(clz.getName(), construction, null, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStart(Class<? super T> clz, Consumer<? super T> start) {
		return define(clz.getName(), null, start, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStop(Class<? super T> clz, Consumer<? super T> stop) {
		return define(clz.getName(), null, null, stop);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStartStop(Class<? super T> clz, Consumer<? super T> start, Consumer<? super T> stop) {
		return define(clz.getName(), null, start, stop);
	}

	/**
	 * Optional, otherwise default constructor is used.
	 * 
	 * @param name         object identifier
	 * @param construction
	 * @return the injector
	 */
	public <T> Wiring defineConstruction(String name, Supplier<? super T> construction) {
		return define(name, construction, null, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStart(String name, Consumer<? super T> start) {
		return define(name, null, start, null);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStop(String name, Consumer<? super T> stop) {
		return define(name, null, null, stop);
	}

	/**
	 * Optional
	 */
	public <T> Wiring defineStartStop(String name, Consumer<? super T> start, Consumer<? super T> stop) {
		return define(name, null, start, stop);
	}

	private <T> Wiring define(String name, Supplier<? super T> construction, Consumer<? super T> start,
			Consumer<? super T> stop) {
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
	 * Creates objects and wires them up. Defined construction supplier or
	 * no-argument constructors are invoked to create objects if not created yet.
	 * Objects are treated as 'singletons. Multiple connects of same classes are
	 * ignored per default, see also {@link #setStrictConnect(boolean)}.
	 */
	public <T, U> Wiring connect(Class<T> classDependent, Class<U> classTarget) throws Exception {
		Dependent dependentObj = (Dependent) getOrCreateObject(classDependent);
		Object targetObj = getOrCreateObject(classTarget);
		connectImpl(targetObj, dependentObj);
		return this;
	}

	private void connectImpl(Object target, Dependent dependent) {
		List<Dependency<?>> dependencies = _WiringHelper.getContext(contextName)
			.getDependencies(dependent);
		wire(dependencies, target);
	}

	public Wiring await() {
		// TODO
		return this;
	}

	public Wiring start() {
		this.state = State.START_IN_PROGRESS;
		objectConstructionSequenceList.forEach(name -> {
			Consumer<?> consumer = objectStartMap.get(name);
			if (consumer != null) {
				consumer.accept(this.getTypedObject(name));
			}
		});
		this.state = State.START_FINISHED;
		return this;
	}

	public Wiring stop() {
		this.state = State.STOP_IN_PROGRESS;
		objectConstructionSequenceList.forEach(name -> {
			Consumer<?> consumer = objectStopMap.get(name);
			if (consumer != null) {
				consumer.accept(this.getTypedObject(name));
			}
		});
		this.state = State.STOP_FINISHED;
		return this;
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

	public <T> T get(String name) {
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

	public void print(PrintStream out) {
		_WiringHelper helper = _WiringHelper.getContext(contextName);
		objectConstructionSequenceList.forEach(name -> {
			Object object = objectMap.get(name);
			if (object instanceof Dependent) {
				List<Dependency<?>> dependencies = helper.getDependencies((Dependent) object);
				String depName = getPrintName(name, object);
				out.print(indent);
				out.println(depName);
				indent += "  ";
				dependencies.forEach(dep -> {
					out.print(indent);
					out.print("-> ");
					Object target = dep.get();
					if (target == null) {
						out.println("UNRESOLVED dependency to: " + dep.getTargetClass()
							.getName());
					} else {
						String targetName = getPrintName(target.getClass()
							.getName(), target);
						out.println(targetName);
					}
				});
				indent = indent.substring(0, indent.length() - 2);
			}
		});
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

	private Object getOrCreateObject(Class<?> clz) throws Exception {
		String name = clz.getName();
		if (!objectMap.containsKey(name)) {
			_WiringHelper.setContext(contextName);
			try {
				final Object newObject;
				if (objectConstructionMap.containsKey(name)) {
					newObject = objectConstructionMap.get(name)
						.get();
				} else {
					newObject = clz.getDeclaredConstructor()
						.newInstance();
				}
				objectMap.put(name, newObject);
				objectConstructionSequenceList.add(name);
			} finally {
				_WiringHelper.setContext(null);
			}
		}
		return objectMap.get(name);

	}

	private void wire(List<Dependency<?>> dependencies, Object dependingOn) {
		for (Dependency<?> dependency : dependencies) {
			if (dependency.get() != null) {
				if (!isStrictConnect && dependency.get()
					.getClass()
					.getName()
					.equals(dependingOn.getClass()
						.getName())) {
					throw new IllegalStateException("Class " + dependency.getDependent()
						.getClass()
						.getName() + " is already connected to "
							+ dependingOn.getClass()
								.getName());
				}
				continue;
			}
			if (dependency.set(dependingOn)) {
				break;
			}
		}
	}

	/**
	 * Clears all contexts.
	 */
	public static void resetAll() {
		wiringContextMap.clear();
		_WiringHelper.restAll();
	}

}
