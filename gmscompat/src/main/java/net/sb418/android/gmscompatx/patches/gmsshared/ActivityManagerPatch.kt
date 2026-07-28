/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.ActivityManager
import android.content.Context
import com.android.internal.gmscompat.GmsHooks
import com.android.internal.gmscompat.sysservice.GmcUserManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose:
 * 1. apply [GmsHooks.addRecentlyBoundPids] to result of [ActivityManager.getRunningAppProcesses]
 * 2. replace some [ActivityManager] methods with [GmcUserManager] hooks
 *
 * Implements:
 * - [94da5aad6c: ActivityManager.java#L3633](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L3633)
 * - [94da5aad6c: ActivityManager.java#L4315](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L4315)
 * - [94da5aad6c: ActivityManager.java#L4546](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L4546)
 */
@Suppress("ClassName")
internal object ActivityManagerPatch : IPatch {
	override fun install() {
		ActivityManager_getRunningAppProcesses.install()
		ActivityManager_getCurrentUser.install()
		ActivityManager_isUserRunning.install()
	}

	/**
	 * Purpose: filter results of [ActivityManager.getRunningAppProcesses] through [GmsHooks.addRecentlyBoundPids].
	 *
	 * Implements: [94da5aad6c: ActivityManager.java#L3633](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L3633)
	 */
	private object ActivityManager_getRunningAppProcesses : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(ActivityManager::class, "getRunningAppProcesses"),
				this
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if exception was thrown
			if (param.hasThrowable()) return

			param.result = GmsHooks.addRecentlyBoundPids(
				XposedHelpers.getObjectField(param.thisObject, "mContext") as Context,
				param.result as List<ActivityManager.RunningAppProcessInfo>
			)
		}
	}

	/**
	 * Implements [94da5aad6c: ActivityManager.java#L4315](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L4315)
	 */
	private object ActivityManager_getCurrentUser : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(ActivityManager::class, "getCurrentUser"),
				this
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam) =
			GmcUserManager.amGetCurrentUser()
	}

	/**
	 * Implements [94da5aad6c: ActivityManager.java#L4546](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityManager.java#L4546)
	 */
	private object ActivityManager_isUserRunning : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(ActivityManager::class, "isUserRunning", Int::class),
				this
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam) =
			GmcUserManager.amIsUserRunning(param.args[0] as Int)
	}
}
