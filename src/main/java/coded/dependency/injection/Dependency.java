package coded.dependency.injection;

import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.CyclicDependencyException;
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
		_WiringHelper helper = _WiringHelper.getContext();
		assert helper != null;
		helper.newDependency(d, this);
		try {
			target = helper.getObject(this, targetClass);
			if (target == null) {
				// internal error
				throw new IllegalStateException(getInjectionInfo(d));
			}
			helper.loginfo(Dependency.class, () -> {
				return "Injected " + getInjectionInfo(d) + ".";
			});
		} catch (BeanOutOfContextCreationException | CyclicDependencyException | ConstructionMissingException e) {
			throw e;
		} catch (Exception e) {
			if (_WiringHelper.isCauseKnownRuntimeException(e)) {
				throw (RuntimeException) e.getCause();
			}
			helper.logerror(Dependency.class, () -> "Injecting " + getInjectionInfo(d) + " failed", e);
			throw new DependencyCreationException(e);
		}
	}

	private String getInjectionInfo(Dependent d) {
		if (d == null) {
			return "dependent is NULL";
		}
		if (target == null) {
			return "target is NULL";
		}
		Class<? extends Object> targetClass = target.getClass();
		return d.getClass()
			.getSimpleName() + " -> " + targetClass.getSimpleName() + " ('" + targetClass.getName()
				+ "' into the dependent '" + d.getClass()
					.getName()
				+ "')";
	}

	public T get() {
		return this.target;
	}

	public Class<T> getTargetClass() {
		return targetClass;
	}

	public void setTarget(T object) {
		target = object;
	}
}
