package coded.dependency.injection;

import coded.dependency.ijection.internal._WiringHelper;

public class Dependency<T> {

	private Dependent dependent;
	private T target;
	private Class<T> targetClass;

	/**
	 * Creates the proxy that connects the dependent (this) to the target class.
	 * 
	 * <pre>
	 * A -> B
	 * 
	 * class A implements Dependent {
	 *   Dependency&lt;B&gt; b = new Dependency<>(this, B.class);
	 *   ...
	 *   B bImpl = b.get();
	 * }
	 * </pre>
	 * 
	 * @param d           this (the dependent)
	 * @param targetClass the target object type (service)
	 */
	public Dependency(Dependent d, Class<T> targetClass) {
		this.dependent = d;
		this.targetClass = targetClass;
		_WiringHelper.getContext()
			.newDependency(d, this);
	}

	public T get() {
		return this.target;
	}

	public Dependent getDependent() {
		return dependent;
	}

	@SuppressWarnings("unchecked")
	boolean set(Object target) {
		if (targetClass.isAssignableFrom(target.getClass())) {
			this.target = (T) target;
			return true;
		} else {
			return false;
		}
	}

	public Class<T> getTargetClass() {
		return targetClass;
	}
}
