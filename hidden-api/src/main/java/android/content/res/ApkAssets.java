package android.content.res;

import android.content.res.loader.AssetsProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public final class ApkAssets {
	// stub: uses IntDef
	@Retention(RetentionPolicy.SOURCE)
	public @interface PropertyFlags {}

	public static @NonNull ApkAssets loadFromFd(@NonNull FileDescriptor fd, @NonNull String friendlyName,
	                                            @PropertyFlags int flags, @Nullable AssetsProvider assets)
			throws IOException {
		throw new RuntimeException("stub");
	}
}
