/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose:
 * 1. call [GmsHooks.activityOnCreate]
 * 2. intercept attempts to start PermissionController activities (privileged)
 *
 * Implements:
 * 1. [94da5aad6c: Activity.java#L1659](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Activity.java#L1659)
 * 2. [94da5aad6c: Activity.java#L5511-L5519](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Activity.java#L5511-L5519)
 */
@Suppress("ClassName")
internal object ActivityPatch : IPatch {
	override fun install() {
		Activity_onCreate.install()
		Activity_startActivityForResult.install()
	}

	private object Activity_onCreate : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(Activity::class, "onCreate", Bundle::class), this)
		}

		override fun beforeHookedMethod(param: MethodHookParam) =
			GmsHooks.activityOnCreate(param.thisObject as Activity)
	}

	private object Activity_startActivityForResult : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					Activity::class, "startActivityForResult",
					Intent::class, Int::class, Bundle::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val intent = param.args[0] as Intent

			val cn = intent.component
			if (cn != null && cn.packageName == "com.google.android.permissioncontroller") {
				// GOS:
				// "PermissionController activities can't be opened by unprivileged apps.
				// (Replacing absent com.google.android.permissioncontroller package with
				// com.android.permissioncontroller would not help)"
				param.result = null
			}
		}
	}
}
