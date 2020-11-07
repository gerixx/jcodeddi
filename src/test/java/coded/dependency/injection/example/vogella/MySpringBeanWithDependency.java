package coded.dependency.injection.example.vogella;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MySpringBeanWithDependency implements Dependent {

	private Dependency<IWriter> writer = new Dependency<>(this, IWriter.class);

	// NOT NEEDED with JCodedDI
	// public void setWriter(IWriter writer) { this.writer = writer; }

	public void run() {
		String s = "This is my test";
		writer.get()
			.writer(s);
	}
}
