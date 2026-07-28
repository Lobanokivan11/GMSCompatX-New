package com.android.permissioncontroller.permission.model.livedatatypes

/**
 * Stub of non-exported class - based on `android-13.0.0_r54`.
 */
data class LightPermission(
	val pkgInfo: LightPackageInfo,
	val permInfo: LightPermInfo,
	val isGrantedIncludingAppOp: Boolean,
	val flags: Int,
	val foregroundPerms: List<String>?
) {
	val name = permInfo.name
}
