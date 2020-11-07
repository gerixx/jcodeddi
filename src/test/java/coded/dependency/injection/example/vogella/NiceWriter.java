package coded.dependency.injection.example.vogella;

public class NiceWriter implements IWriter {

	@Override
	public void writer(String s) {
		System.out.println("The string is " + s);
	}

}
