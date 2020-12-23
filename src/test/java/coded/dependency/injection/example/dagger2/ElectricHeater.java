package coded.dependency.injection.example.dagger2;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

public class ElectricHeater implements Heater, Dependent {

	private final CoffeeLogger logger = new Dependency<CoffeeLogger>(this, CoffeeLogger.class).get();
	private boolean heating;

	@Override
	public void on() {
		this.heating = true;
		logger.log("~ ~ ~ heating ~ ~ ~");
	}

	@Override
	public void off() {
		this.heating = false;
	}

	@Override
	public boolean isHot() {
		return heating;
	}
}