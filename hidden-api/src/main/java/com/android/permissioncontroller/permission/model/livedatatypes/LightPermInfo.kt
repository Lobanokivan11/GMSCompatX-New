package com.android.permissioncontroller.permission.model.livedatatypes

/**
 * Stub of non-exported class - based on `android-13.0.0_r54`.
 */
data class LightPermInfo(
	val name: String,
	val packageName: String,
	val group: String?,
	val backgroundPermission: String?,
	val protection: Int,
	val protectionFlags: Int,
	val flags: Int
)
