package coded.dependency.injection;

public class CyclicDependencyException extends RuntimeException {

	private static final long serialVersionUID = 4011952158270004353L;

	public CyclicDependencyException(String msg) {
		super(msg);
	}

}
