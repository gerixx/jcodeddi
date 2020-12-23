package coded.dependency.injection.example.dagger2;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

//TODO extend Dependency to allow lazy instantiation, to create a possibly costly heater only when we use it.

public class CoffeeMaker implements Dependent {
	private final CoffeeLogger logger = new Dependency<>(this, CoffeeLogger.class).get();
	private final Heater heater = new Dependency<>(this, Heater.class).get();
	private final Pump pump = new Dependency<>(this, Pump.class).get();

	public void brew() {
		heater.on();
		pump.pump();
		logger.log(" [_]P coffee! [_]P ");
		heater.off();
	}
}
