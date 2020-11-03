package coded.dependency.ijection.example.vogella;

import coded.dependency.injection.Wiring;

public class Main {

	public static void main(String[] args) {
		Wiring injector = Wiring.getContext("main")
			.defineConstruction(IWriter.class, NiceWriter::new) // inject NiceWriter by defining a supplier for the
																// IWriter bean
			.connectAll(MySpringBeanWithDependency.class);

		MySpringBeanWithDependency test = injector.get(MySpringBeanWithDependency.class); // no cast needed
		test.run();
	}
}
