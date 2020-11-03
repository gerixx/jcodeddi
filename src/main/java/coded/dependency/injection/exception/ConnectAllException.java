package coded.dependency.injection.exception;

public class ConnectAllException extends RuntimeException {

	private static final long serialVersionUID = -3527050627149796478L;

	public ConnectAllException(Exception e) {
		super(e);
	}

}
