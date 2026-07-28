package android.os;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public class PowerExemptionManager {
	public static int TEMPORARY_ALLOW_LIST_TYPE_NONE;
	public static int REASON_UNKNOWN;

	public static String reasonCodeToString(@ReasonCode int reasonCode) {
		throw new RuntimeException("stub");
	}

	// stub: original uses IntDef
	@Retention(RetentionPolicy.SOURCE)
	public @interface ReasonCode {}

	// stub: original uses IntDef
	@Retention(RetentionPolicy.SOURCE)
	public @interface TempAllowListType {}
}
