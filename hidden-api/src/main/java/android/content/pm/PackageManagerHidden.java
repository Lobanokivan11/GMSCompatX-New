package android.content.pm;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(PackageManager.class)
public abstract class PackageManagerHidden {
	public static int DELETE_FAILED_ABORTED;
	public static int MATCH_ANY_USER;
	public static int INSTALL_FAILED_SESSION_INVALID;

	public interface OnPermissionsChangedListener {
		public void onPermissionsChanged(int uid);
	}
}
