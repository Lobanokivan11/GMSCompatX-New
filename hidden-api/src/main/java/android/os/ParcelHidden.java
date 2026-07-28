package android.os;

import android.util.ArraySet;

import androidx.annotation.Nullable;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(Parcel.class)
public final class ParcelHidden {
	public @Nullable ArraySet<?> readArraySet(@Nullable ClassLoader loader) {
		throw new RuntimeException("stub");
	}
	public void writeArraySet(@Nullable ArraySet<?> val) {
		throw new RuntimeException("stub");
	}
	public final @Nullable String[] readStringArray() {
		throw new RuntimeException("stub");
	}
}
