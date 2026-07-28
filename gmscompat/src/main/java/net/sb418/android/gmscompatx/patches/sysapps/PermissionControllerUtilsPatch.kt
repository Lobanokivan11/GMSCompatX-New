/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.sysapps

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Application
import android.app.compat.gms.GmsCompat
import android.os.UserHandle
import android.os.UserHandleHidden
import com.android.internal.gmscompat.GmsInfo
import com.android.permissioncontroller.permission.model.livedatatypes.LightAppPermGroup
import com.android.permissioncontroller.permission.model.livedatatypes.LightPermission
import com.android.permissioncontroller.permission.utils.KotlinUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: restart GMS apps when a new permission is granted.
 *
 * Implements: [modules/Permission: 093b018cbf](https://github.com/GrapheneOS/platform_packages_modules_Permission/commit/093b018cbf50f4410ea04b0ed143662b04897158)
 */
internal object PermissionControllerUtilsPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(KotlinUtils::class, "grantRuntimePermission",
				Application::class, LightPermission::class, Boolean::class, LightAppPermGroup::class),
			this
		)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception thrown
		if (param.hasThrowable()) return

		val perm = param.args[1] as LightPermission
		val group = param.args[3] as LightAppPermGroup
		val pkgInfo = group.packageInfo

		@Suppress("CAST_NEVER_SUCCEEDS")
		val user = UserHandle.getUserHandleForUid(pkgInfo.uid) as UserHandleHidden

		if (GmsCompat.isGmsApp(pkgInfo.packageName, user.identifier)) {
			if (pkgInfo.packageName != GmsInfo.PACKAGE_GSA && perm.name != POST_NOTIFICATIONS) {
				// set shouldKill to true
				val (newPerm: LightPermission, _) = param.result as Pair<LightPermission, Boolean>
				param.result = Pair(newPerm, true)
			}
		}
	}
}
