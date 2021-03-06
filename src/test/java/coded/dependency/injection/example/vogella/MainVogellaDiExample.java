package coded.dependency.injection.example.vogella;

import coded.dependency.injection.Injector;

public class MainVogellaDiExample {

	public static void main(String[] args) {
		Injector injector = Injector.getContext("main")
			.defineConstruction(IWriter.class, NiceWriter::new) // inject NiceWriter by defining a supplier for the
																// IWriter bean
			.makeBeans(MySpringBeanWithDependency.class);

		MySpringBeanWithDependency test = injector.getBean(MySpringBeanWithDependency.class); // no cast needed
		test.run();
	}
}
