package coded.dependency.injection.example.vogella;

public class Writer implements IWriter {

	@Override
	public void writer(String s) {
		System.out.println(s);
	}

}
