package android.location;

import android.os.WorkSource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(LocationRequest.class)
public final class LocationRequestHidden /* implements Parcelable */ {
	public void setHideFromAppOps(boolean hiddenFromAppOps) {
		throw new RuntimeException("stub");
	}

	public @NonNull LocationRequest setLocationSettingsIgnored(boolean locationSettingsIgnored) {
		throw new RuntimeException("stub");
	}

	public void setWorkSource(@Nullable WorkSource workSource) {
		throw new RuntimeException("stub");
	}
}
