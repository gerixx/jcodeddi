package coded.dependency.injection;

import java.io.PrintWriter;
import java.util.function.Supplier;

public class _LogBindingAdapterCapture extends LogBindingAdapter {

	public _LogBindingAdapterCapture(PrintWriter out) {
		super(out);
	}

	@Override
	protected void print(String level, String contextName, String fileName, int lineNumber,
			Supplier<String> msgSupplier) {
		super.print(level, contextName, fileName, lineNumber, msgSupplier);
	}

}
