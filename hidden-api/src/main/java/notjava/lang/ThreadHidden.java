package notjava.lang;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(Thread.class)
public class ThreadHidden /* implements Runnable */ {
	public static void setUncaughtExceptionPreHandler(Thread.UncaughtExceptionHandler eh) {
		throw new RuntimeException("stub");
	}

	public static Thread.UncaughtExceptionHandler getUncaughtExceptionPreHandler() {
		throw new RuntimeException("stub");
	}
}
