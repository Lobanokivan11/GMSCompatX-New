package com.android.server.pm.pkg;

import androidx.annotation.NonNull;

import com.android.server.pm.parsing.pkg.AndroidPackage;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public interface PackageStateInternal extends PackageState {
	@NonNull AndroidPackage getPkg();
}
