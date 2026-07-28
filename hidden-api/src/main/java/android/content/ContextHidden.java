package android.content;

import android.annotation.UserIdInt;
import android.os.Handler;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(Context.class)
public abstract class ContextHidden {
	public @UserIdInt int getUserId() { throw new RuntimeException("stub"); }
	public Handler getMainThreadHandler() { throw new RuntimeException("stub"); }

	public static int BIND_ALLOW_BACKGROUND_ACTIVITY_STARTS;

	// stub: these are re-defined here because GmsHooks#isHiddenSystemService requires them to be constant
	public static final String CONTEXTHUB_SERVICE = "contexthub";
	public static final String WIFI_SCANNING_SERVICE = "wifiscanner";
	public static final String APP_INTEGRITY_SERVICE = "app_integrity";
	public static final String PERSISTENT_DATA_BLOCK_SERVICE = "persistent_data_block";
	public static final String FONT_SERVICE = "font";
	public static final String STATS_MANAGER = "stats";
}
