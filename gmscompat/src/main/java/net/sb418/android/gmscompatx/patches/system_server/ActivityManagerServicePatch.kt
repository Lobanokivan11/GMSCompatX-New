/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.ManifestHidden
import android.app.ActivityManagerHidden
import android.app.compat.gms.GmsCompat
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Process
import android.os.UserHandleHidden
import com.android.server.am.ActivityManagerService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: spoof [ActivityManagerService.isSingleton] to avoid throwing [SecurityException]s due to
 * missing permissions when GMS attempts to use `FLAG_SINGLE_USER`.
 *
 * Implements: [94da5aad6c: ActivityManagerService.java#L12662-L12664](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/services/core/java/com/android/server/am/ActivityManagerService.java#L12662-L12664)
 */
internal object ActivityManagerServicePatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				ActivityManagerService::class, "isSingleton",
				String::class, ApplicationInfo::class, String::class, Int::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val aInfo = param.args[1] as ApplicationInfo
		val flags = param.args[3] as Int

		// duplicate the original checks, plus our `isGmsApp` check
		if (UserHandleHidden.getAppId(aInfo.uid) >= Process.FIRST_APPLICATION_UID) {
			if (flags.and(ServiceInfo.FLAG_SINGLE_USER) != 0) {
				if (GmsCompat.isGmsApp(aInfo)) {
					if (ActivityManagerHidden.checkUidPermission(
							ManifestHidden.permission.INTERACT_ACROSS_USERS, aInfo.uid
						) != PackageManager.PERMISSION_GRANTED
					) {
						param.result = false
					}
				}
			}
		}
	}
}
