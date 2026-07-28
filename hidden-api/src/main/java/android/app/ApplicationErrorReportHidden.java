package android.app;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(ApplicationErrorReport.class)
public class ApplicationErrorReportHidden /* implements Parcelable */ {
	public static class ParcelableCrashInfo extends ApplicationErrorReport.CrashInfo /* implements Parcelable */ {
		public ParcelableCrashInfo(Throwable tr) {
			throw new RuntimeException("stub");
		}
	}
}
