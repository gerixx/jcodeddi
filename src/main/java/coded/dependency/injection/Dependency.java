package coded.dependency.injection;

import coded.dependency.injection.internal.DependencyCreationException;
import coded.dependency.injection.internal._WiringHelper;

public class Dependency<T> {

	private T target;
	private Class<T> targetClass;

	/**
	 * Creates the proxy that connects the dependent (this) to the target (service).
	 * Dependencies of the target class are created recursively.
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
		this.targetClass = targetClass;
		_WiringHelper helperContext = _WiringHelper.getContext();
		assert helperContext != null;
		helperContext.newDependency(d, this);
		try {
			target = helperContext.getObject(targetClass);
		} catch (Exception e) {
			throw new DependencyCreationException(e);
		}
	}

	public T get() {
		return this.target;
	}

	public Class<T> getTargetClass() {
		return targetClass;
	}
}
