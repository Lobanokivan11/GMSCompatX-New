/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.app.ApplicationPackageManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.android.internal.gmscompat.sysservice.GmcPackageManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: spoof package info by applying [GmcPackageManager] adjustments to [ApplicationPackageManager] methods.
 *
 * Implements:
 * - [94da5aad6c: ApplicationPackageManager.java#L258](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ApplicationPackageManager.java#L258)
 * - [94da5aad6c: ApplicationPackageManager.java#L511](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ApplicationPackageManager.java#L511)
 */
@Suppress("ClassName")
internal object ApplicationPackageManagerPatch : IPatch {
	override fun install() {
		ApplicationPackageManager_getPackageInfoAsUser.install()
		ApplicationPackageManager_getApplicationInfoAsUser.install()
	}

	private object ApplicationPackageManager_getPackageInfoAsUser : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ApplicationPackageManager::class, "getPackageInfoAsUser",
					String::class, PackageManager.PackageInfoFlags::class, Int::class),
				this,
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			GmcPackageManager.maybeAdjustPackageInfo(param.result as PackageInfo)
		}
	}

	private object ApplicationPackageManager_getApplicationInfoAsUser : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ApplicationPackageManager::class, "getApplicationInfoAsUser",
					String::class, PackageManager.ApplicationInfoFlags::class, Int::class),
				this,
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			GmcPackageManager.maybeAdjustApplicationInfo(param.result as ApplicationInfo)
		}
	}
}
