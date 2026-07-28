package android.telephony;

import android.content.Context;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(TelephonyManager.class)
// stub: extends original class to simplify GmcTelephonyManager implementation
public class TelephonyManagerHidden extends TelephonyManager {
	public TelephonyManagerHidden(Context ctx) {
		throw new RuntimeException("stub");
	}

	public TelephonyManagerHidden(Context ctx, int subId) {
		throw new RuntimeException("stub");
	}
}
