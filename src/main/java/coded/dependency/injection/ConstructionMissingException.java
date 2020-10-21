package coded.dependency.injection;

public class ConstructionMissingException extends RuntimeException {

	private static final long serialVersionUID = 1868997471629132596L;

	public ConstructionMissingException(String msg) {
		super(msg);
	}

}
