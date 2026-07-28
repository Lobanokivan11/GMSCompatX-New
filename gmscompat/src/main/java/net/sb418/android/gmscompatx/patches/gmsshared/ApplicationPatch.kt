/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.Application
import com.android.internal.gmscompat.util.GmcActivityUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: register [GmcActivityUtils] activity lifecycle callbacks.
 *
 * Implements [94da5aad6c: Application.java#L238](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Application.java#L238)
 */
internal object ApplicationPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findConstructorExactKt(Application::class), this)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception thrown
		if (param.hasThrowable()) return

		val thisObj = param.thisObject as Application

		thisObj.registerActivityLifecycleCallbacks(GmcActivityUtils.INSTANCE)
	}
}
