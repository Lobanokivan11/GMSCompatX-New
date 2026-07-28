package com.android.permissioncontroller.permission.model.livedatatypes

/**
 * Stub of non-exported class - based on `android-13.0.0_r54`.
 */
data class LightAppPermGroup(
	val packageInfo: LightPackageInfo,
	val permGroupInfo: LightPermGroupInfo,
	val allPermissions: Map<String, LightPermission>,
	val hasInstallToRuntimeSplit: Boolean,
	val specialLocationGrant: Boolean?
)
