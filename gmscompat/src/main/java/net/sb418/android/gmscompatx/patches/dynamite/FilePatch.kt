/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.dynamite

import com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.io.File

/**
 * Purpose: apply [GmsDynamiteClientHooks.getFileLastModified]
 * to prevent spurious "APK has been modified" errors with Dynamite modules.
 *
 * Implements: [7a7852258b: File.java#L956-L963](https://github.com/GrapheneOS/platform_libcore/blob/7a7852258b52e155e59dfc5b5769acdd6a811e37/ojluni/src/main/java/java/io/File.java#L956-L963)
 */
internal object FilePatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(File::class, "lastModified"), this)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception was thrown
		if (param.hasThrowable()) return

		if (param.result as Long == 0L) {
			param.result = GmsDynamiteClientHooks.getFileLastModified(param.thisObject as File)
		}
	}
}
