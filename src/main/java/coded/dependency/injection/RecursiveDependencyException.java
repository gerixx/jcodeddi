package coded.dependency.injection;

public class RecursiveDependencyException extends RuntimeException {

	private static final long serialVersionUID = 4011952158270004353L;

	public RecursiveDependencyException(String msg) {
		super(msg);
	}

}
