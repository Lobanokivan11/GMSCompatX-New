/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.annotation.SuppressLint
import android.util.ArraySet
import com.android.internal.gmscompat.GmsCompatApp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: automatically give location and notification permissions to GmsCompatApp.
 *
 * Implements: [94da5aad6c: DefaultPermissionGrantPolicy.java#L820-L821](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/services/core/java/com/android/server/pm/permission/DefaultPermissionGrantPolicy.java#L820-L821)
 */
internal object DefaultPermissionGrantPolicyPatch : IPatch, XC_MethodHook() {
	@SuppressLint("PrivateApi")
	private val classDefaultPermissionGrantPolicy =
		Class.forName("com.android.server.pm.permission.DefaultPermissionGrantPolicy").kotlin
	@SuppressLint("PrivateApi")
	private val classPackageManagerWrapper =
		Class.forName("com.android.server.pm.permission.DefaultPermissionGrantPolicy\$PackageManagerWrapper").kotlin

	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				classDefaultPermissionGrantPolicy, "grantDefaultSystemHandlerPermissions",
				classPackageManagerWrapper, Int::class,
			),
			this
		)
	}

	private val methodGrantPermissionsToSystemPackage =
		MethodFinder.findMethodExactKt(
			classDefaultPermissionGrantPolicy, "grantPermissionsToSystemPackage",
			classPackageManagerWrapper, String::class, Int::class, MethodFinder.arrayType(java.util.Set::class),
		)

	private val ALWAYS_LOCATION_PERMISSIONS =
		XposedHelpers.getStaticObjectField(classDefaultPermissionGrantPolicy.java, "ALWAYS_LOCATION_PERMISSIONS") as ArraySet<String>
	private val NOTIFICATION_PERMISSIONS =
		XposedHelpers.getStaticObjectField(classDefaultPermissionGrantPolicy.java, "NOTIFICATION_PERMISSIONS") as ArraySet<String>

	override fun beforeHookedMethod(param: MethodHookParam) {
		assert(classDefaultPermissionGrantPolicy.isInstance(param.thisObject))
		assert(classPackageManagerWrapper.isInstance(param.args[0]))

		methodGrantPermissionsToSystemPackage.invoke(
			param.thisObject,
			param.args[0],
			GmsCompatApp.PKG_NAME,
			param.args[1] as Int,
			ALWAYS_LOCATION_PERMISSIONS, NOTIFICATION_PERMISSIONS,
		)
	}
}
