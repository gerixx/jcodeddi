package coded.dependency.injection.example.springfw.guru;

import java.util.List;

import coded.dependency.injection.Injector;

public class DiExampleApplication {

	public static void main(String[] args) {
		Injector injector = Injector.getContext("main")
			.defineConstruction(ProductService.class, ProductServiceImpl::new)
			.makeBeans(MyController.class);

		List<Product> products = injector.getBean(MyController.class)
			.getProducts();

		for (Product product : products) {
			System.out.println(product.getDescription());
		}

		System.out.println();
		injector.print();
	}
}
