package coded.dependency.injection.exception;

public class BeanOutOfContextCreationException extends RuntimeException {

	private static final long serialVersionUID = -7214611224893095624L;

	public BeanOutOfContextCreationException() {
		super();
	}

	public BeanOutOfContextCreationException(String msg) {
		super(msg);
	}

}
