package com.android.server.pm.parsing.pkg;

import androidx.annotation.Nullable;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public interface AndroidPackageApi {
	boolean isPrivileged();

	@Nullable String getSharedUserId();

	long getLongVersionCode();
}
