/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.playstore

import android.app.usage.StorageStatsManager
import android.content.pm.PackageManager
import android.os.UserHandle
import com.android.internal.gmscompat.GmsInfo
import com.android.internal.gmscompat.PlayStoreHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.UUID

/**
 * Purpose: spoof package storage stats to Play Store.
 *
 * Implements: [94da5aad6c: StorageStatsManager.java#L214-L218](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/usage/StorageStatsManager.java#L214-L218)
 */
internal object StorageStatsManagerPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				StorageStatsManager::class, "queryStatsForPackage",
				UUID::class, String::class, UserHandle::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val packageName = param.args[1] as String

		if (packageName != GmsInfo.PACKAGE_PLAY_STORE) {
			try {
				param.result = PlayStoreHooks.queryStatsForPackage(packageName)
			} catch (e: PackageManager.NameNotFoundException) {
				param.throwable = e
			}
		}
	}
}
