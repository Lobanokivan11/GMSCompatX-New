/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.playstore

import android.content.pm.PackageInstaller
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: set `USER_ACTION_NOT_REQUIRED` to enable updating apps without user confirmation
 * (under [certain circumstances](https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams#setRequireUserAction(int))).
 *
 * Implements: [94da5aad6c: PackageInstaller.java#L1838](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/pm/PackageInstaller.java#L1838)
 */
internal object EnableSilentAppUpdatesPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findConstructorExactKt(PackageInstaller.SessionParams::class, Int::class),
			this
		)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception thrown
		if (param.hasThrowable()) return

		val thisObj = param.thisObject as PackageInstaller.SessionParams

		// GOS: "called here instead of in createSession() to give Play Store a chance to override"
		thisObj.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
	}
}

