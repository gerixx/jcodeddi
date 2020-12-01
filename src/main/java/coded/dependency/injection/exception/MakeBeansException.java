package coded.dependency.injection.exception;

public class MakeBeansException extends RuntimeException {

	private static final long serialVersionUID = -3527050627149796478L;

	public MakeBeansException(Exception e) {
		super(e);
	}

}
