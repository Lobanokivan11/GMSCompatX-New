/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.playstore

import com.android.internal.gmscompat.PlayStoreHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.io.File

/**
 * Purpose: patch [File.mkdirs] to trigger a "missing permission" notification.
 *
 * Implements: [7a7852258b: File.java#L1387-1396](https://github.com/GrapheneOS/platform_libcore/commit/7a7852258b52e155e59dfc5b5769acdd6a811e37#diff-8935cf71fcf9ae8d1208e6413e9467eca3bd15f8537e81fac65c4ae820177261R1387-R1396)
 */
internal object FilePatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(File::class, "mkdirs"), this)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception thrown
		if (param.hasThrowable()) return

		if (!(param.result as Boolean)) {
			PlayStoreHooks.mkdirsFailed(param.thisObject as File)
		}
	}
}
