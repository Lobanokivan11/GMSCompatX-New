/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.app.Instrumentation
import android.app.compat.gms.GmsCompat
import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.patches.UniversalPatches.TAG

/**
 * Purpose: enable GmsCompat just before the [android.app.Application] is instantiated.
 *
 * Implements:
 * - [94da5aad6c: Instrumentation.java#L1245](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Instrumentation.java#L1245)
 * - [94da5aad6c: Instrumentation.java#L1264](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Instrumentation.java#L1264)
 */
internal object InstrumentationPatch : IPatch, XC_MethodHook() {
	override fun install() {
		// hook all overloads of Instrumentation#newApplication
		XposedBridge.hookAllMethods(Instrumentation::class.java, "newApplication", this)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		// NOTE: there are two implementations of newApplication, so we scan the arguments for the Context
		for (arg in param.args) {
			if (arg is Context) {
				try {
					val gmsHooksClass = GmsHooks::class.java
					val hasNewApi = gmsHooksClass.declaredMethods.any { method ->
						method.parameterTypes.any { paramType ->
							paramType.name.contains("IFileProxyService")
						}
					}
					val hasNewGmsCompatApp = try {
						Class.forName("app.grapheneos.gmscompat.BinderClientOfGmsCore2Gca", false, arg.classLoader)
						true
					} catch (e: ClassNotFoundException) {
						false
					}
					if (hasNewApi && !hasNewGmsCompatApp) {
						Log.e(TAG, "CRITICAL: GmsCompat version mismatch detected! Blocked 'GmsCompat.maybeEnable' to prevent AbstractMethodError crash.")
						return
					}
					GmsCompat.maybeEnable(arg)

				} catch (t: Throwable) {
					Log.e(TAG, "Failed to verify GmsCompat signatures safely, falling back", t)
					try { GmsCompat.maybeEnable(arg) } catch (e: Throwable) {}
				}
				return
			}
		}

		Log.e(TAG, "newApplication() hook: failed to locate Context in argument list!")
	}
}
