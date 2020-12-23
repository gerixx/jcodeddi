package coded.dependency.injection.example.dagger2;

import java.util.ArrayList;
import java.util.List;

public final class CoffeeLogger {
	private final List<String> logs = new ArrayList<>();

	public void log(String msg) {
		logs.add(msg);
	}

	public List<String> logs() {
		return new ArrayList<>(logs);
	}
}
