package coded.dependency.injection;

import coded.dependency.injection.exception.BeanOutOfContextCreationException;
import coded.dependency.injection.exception.ConstructionMissingException;
import coded.dependency.injection.exception.ContextMismatchException;
import coded.dependency.injection.exception.CyclicDependencyException;
import coded.dependency.injection.exception.DependencyCreationException;
import coded.dependency.injection.internal._NoContextDefinedException;
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
		registerDependencyForContext(d, targetClass);
	}

	private void registerDependencyForContext(Dependent dependent, Class<T> targetClass) {
		_WiringHelper helper = null;
		try {
			helper = _WiringHelper.getThreadContext();
		} catch (_NoContextDefinedException e) {
			throw new BeanOutOfContextCreationException("injection error: " + getInjectionInfo(dependent)
					+ " - two possible reasons: (1) the bean is not created by the injector or "
					+ "(2) the bean is not created within the injector thread.");
		}

		try {
			helper.addNewDependency(dependent, this);
			target = helper.getObject(targetClass);
			if (target == null) {
				throw new DependencyCreationException(getInjectionInfo(dependent));
			}
			helper.loginfo(Dependency.class, () -> {
				return "Injected " + getInjectionInfo(dependent) + ".";
			});
		} catch (BeanOutOfContextCreationException | ContextMismatchException | CyclicDependencyException
				| ConstructionMissingException | DependencyCreationException e) {
			helper.logerror(Dependency.class, () -> e.getMessage());
			throw e;
		} catch (Exception e) {
			if (_WiringHelper.isCauseKnownRuntimeException(e)) {
				RuntimeException cause = (RuntimeException) e.getCause();
				helper.logerror(Dependency.class, () -> cause.getMessage());
				throw cause;
			}
			helper.logerror(Dependency.class, () -> "Injecting " + getInjectionInfo(dependent) + " failed", e);
			throw new DependencyCreationException(getInjectionInfo(dependent), e);
		}
	}

	/**
	 * Creates a dependency of an anonymous client object. The client must not be
	 * created by {@link Injector#makeBeans(Class)}. With that the client object is
	 * an unknown bean for the injection context. This is not intended for regular
	 * use! But it could help for example during migration to jcodeddi.
	 * 
	 * @param contextName Injector context is implicitly created if not existing yet
	 * @param dependent
	 * @param targetClass
	 */
	public Dependency(String contextName, Dependent dependent, Class<T> targetClass) {
		final _WiringHelper helper = (_WiringHelper) _WiringHelper.getOrCreateContext(contextName);
		try {
			try {
				_WiringHelper.setThreadContext(contextName);
			} catch (ContextMismatchException e) {
				helper.logerror(Dependency.class, () -> e.getMessage());
				throw e;
			}
			registerDependencyForContext(dependent, targetClass);
		} finally {
			_WiringHelper.resetThreadContext();
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
