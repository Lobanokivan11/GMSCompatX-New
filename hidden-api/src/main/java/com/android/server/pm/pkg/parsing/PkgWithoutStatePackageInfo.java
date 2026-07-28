package com.android.server.pm.pkg.parsing;

import androidx.annotation.NonNull;

import com.android.server.pm.pkg.component.ParsedPermission;

import java.util.List;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public interface PkgWithoutStatePackageInfo {
	String getPackageName();
	@NonNull List<ParsedPermission> getPermissions();
}
