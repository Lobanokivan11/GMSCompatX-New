/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.io.File

/**
 * Purpose: apply [GmsHooks.maybeModifySharedPreferencesValues] to return value of [SharedPreferencesImpl.getAll].
 *
 * Implements: [94da5aad6c: SharedPreferencesImpl.java#L299-L307](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/SharedPreferencesImpl.java#L299-L307)
 */
internal object SharedPreferencesImplPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt("android.app.SharedPreferencesImpl", "getAll"),
			this
		)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception was thrown
		if (param.hasThrowable()) return

		// SharedPreferencesImpl is not public, so we have to use reflection
		val mFile = XposedHelpers.getObjectField(param.thisObject, "mFile") as File

		val fileName = mFile.name
		val suffix = ".xml"

		if (fileName.endsWith(suffix)) {
			val name = fileName.removeSuffix(suffix)
			GmsHooks.maybeModifySharedPreferencesValues(name, param.result as HashMap<String, Any>)
		}
	}
}
