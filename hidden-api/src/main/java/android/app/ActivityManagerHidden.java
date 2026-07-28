package android.app;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(ActivityManager.class)
public class ActivityManagerHidden {
	public static int START_ABORTED;
	public static int START_SUCCESS;

	public static int checkUidPermission(String permission, int uid) {
		throw new RuntimeException("stub");
	}
}
