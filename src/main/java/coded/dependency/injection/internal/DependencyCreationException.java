package coded.dependency.injection.internal;

public class DependencyCreationException extends RuntimeException {

	private static final long serialVersionUID = -1917387133638187941L;

	public DependencyCreationException(Exception e) {
		super(e);
	}

}