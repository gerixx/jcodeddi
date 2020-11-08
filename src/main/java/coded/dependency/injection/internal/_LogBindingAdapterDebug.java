package coded.dependency.injection.internal;

import java.io.PrintWriter;

import coded.dependency.injection.LogBindingAdapter;

public class _LogBindingAdapterDebug extends LogBindingAdapter {

	public _LogBindingAdapterDebug(PrintWriter out) {
		super(out);
	}

	public _LogBindingAdapterDebug() {
		this(new PrintWriter(System.out));
	}

	@Override
	protected StackTraceElement getStackTraceElement(StackTraceElement[] stackTrace) {
		boolean adapterBeginFound = false;
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement elem = stackTrace[i];
			if (adapterBeginFound) {
				if (!elem.getClassName()
					.equals(LogBindingAdapter.class.getName())) {
					// adapter end found
					return stackTrace[i + 1];
				}
			} else if (elem.getClassName()
				.equals(LogBindingAdapter.class.getName())) {
				adapterBeginFound = true;
			}
		}
		throw new IllegalStateException(stackTrace.toString());
	}
}
