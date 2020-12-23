package coded.dependency.injection.example.dagger2;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;
import coded.dependency.injection.Injector;

public class CoffeeApp {

	public static class CoffeeShop implements Dependent {
		CoffeeMaker maker = new Dependency<>(this, CoffeeMaker.class).get();
		CoffeeLogger logger = new Dependency<>(this, CoffeeLogger.class).get();

		CoffeeMaker maker() {
			return maker;
		}

		CoffeeLogger logger() {
			return logger;
		}
	}

	public static void main(String[] args) {
		CoffeeShop coffeeShop = Injector.getContext("coffee")
			.defineConstruction(CoffeeShop.class, CoffeeShop::new)
			.defineConstruction(Heater.class, ElectricHeater::new)
			.defineConstruction(Pump.class, Thermosiphon::new)
			.makeBeans(CoffeeShop.class)
			.getBean(CoffeeShop.class);

		Injector.getContext("coffee")
			.print();

		coffeeShop.maker()
			.brew();
		coffeeShop.logger()
			.logs()
			.forEach(log -> System.out.println(log));

	}

}
