package coded.dependency.injection.exception;

/**
 * Unexpected exception, indicates an internal issue.
 *
 */
public class DependencyCreationException extends RuntimeException {

	private static final long serialVersionUID = -1917387133638187941L;

	public DependencyCreationException(String msg) {
		super(msg);
	}

	public DependencyCreationException(String msg, Exception e) {
		super(msg, e);
	}

}
