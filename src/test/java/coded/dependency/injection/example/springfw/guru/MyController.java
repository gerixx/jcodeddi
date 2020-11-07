package coded.dependency.injection.example.springfw.guru;

import java.util.List;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class MyController implements Dependent {

	private Dependency<ProductService> productService = new Dependency<>(this, ProductService.class);

	/*
	 * NOT NEEDED with JCodedDI
	 * 
	 * @Autowired public void setProductService(ProductService productService) {
	 * this.productService = productService; }
	 */

	public List<Product> getProducts() {
		return productService.get()
			.listProducts();
	}
}
