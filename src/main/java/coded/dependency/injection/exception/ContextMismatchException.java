package coded.dependency.injection.exception;

public class ContextMismatchException extends IllegalStateException {

	private static final long serialVersionUID = 2176642788124950619L;

	public ContextMismatchException(String msg) {
		super(msg);
	}

}
