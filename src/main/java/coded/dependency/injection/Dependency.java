package coded.dependency.injection;

import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.exception.DependencyCreationException;
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
	 *   B bImpl = b.get(); // inlining b would be possible
	 * }
	 * </pre>
	 * 
	 * @param d           this (the dependent)
	 * @param targetClass the target object type (service)
	 */
	public Dependency(Dependent d, Class<T> targetClass) {
		this.targetClass = targetClass;
		_WiringHelper helper = null;
		try {
			helper = _WiringHelper.getThreadContext();
			assert helper != null;
			helper.addNewDependency(d, this);
			target = helper.getObject(this, targetClass);
			if (target == null) {
				throw new DependencyCreationException(getInjectionInfo(d));
			}
			helper.loginfo(Dependency.class, () -> {
				return "Injected " + getInjectionInfo(d) + ".";
			});
		} catch (BeanOutOfContextCreationException e) {
			throw new BeanOutOfContextCreationException("injection error: " + getInjectionInfo(d)
					+ " - two possible reasons: (1) the bean is not created by the injector or "
					+ "(2) the bean is not created within the injector thread.");
		} catch (CyclicDependencyException | ConstructionMissingException | DependencyCreationException e) {
			helper.logerror(Dependency.class, () -> e.getMessage());
			throw e;
		} catch (Exception e) {
			if (_WiringHelper.isCauseKnownRuntimeException(e)) {
				throw (RuntimeException) e.getCause();
			}
			if (helper != null) {
				helper.logerror(Dependency.class, () -> "Injecting " + getInjectionInfo(d) + " failed", e);
			}
			throw new DependencyCreationException(getInjectionInfo(d), e);
		}
	}

	private String getInjectionInfo(Dependent d) {
		if (d == null) {
			return "dependent NULL";
		}
		if (target == null) {
			return "target NULL";
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
