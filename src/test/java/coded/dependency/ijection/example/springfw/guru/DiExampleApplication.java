package coded.dependency.ijection.example.springfw.guru;

import java.util.List;

import coded.dependency.injection.Wiring;

public class DiExampleApplication {

	public static void main(String[] args) {
		Wiring injector = Wiring.getContext("main")
			.setLogger(null) // disable logging
			.defineConstruction(ProductService.class, ProductServiceImpl::new)
			.connectAll(MyController.class);

		List<Product> products = injector.get(MyController.class)
			.getProducts();

		for (Product product : products) {
			System.out.println(product.getDescription());
		}

		System.out.println();
		injector.print();
	}
}