package com.android.permissioncontroller.permission.model.livedatatypes

/**
 * Stub of non-exported class - based on `android-13.0.0_r54`.
 */
data class LightPackageInfo(
	val packageName: String,
	val permissions: List<LightPermInfo>,
	val requestedPermissions: List<String>,
	val requestedPermissionsFlags: List<Int>,
	val uid: Int,
	val targetSdkVersion: Int,
	val isInstantApp: Boolean,
	val enabled: Boolean,
	val appFlags: Int,
	val firstInstallTime: Long
)
