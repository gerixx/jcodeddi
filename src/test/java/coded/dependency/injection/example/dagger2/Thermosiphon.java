package coded.dependency.injection.example.dagger2;

import coded.dependency.injection.Dependency;
import coded.dependency.injection.Dependent;

/** A thermosiphon to pump the coffee. */
public class Thermosiphon implements Pump, Dependent {
	private final CoffeeLogger logger = new Dependency<CoffeeLogger>(this, CoffeeLogger.class).get();
	private final Heater heater = new Dependency<Heater>(this, Heater.class).get();

	@Override
	public void pump() {
		if (heater.isHot()) {
			logger.log("=> => pumping => =>");
		}
	}
}