/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.GMSPatches.TAG
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.WeakHashMap

/**
 * Purpose: call [GmsHooks.onActivityStart].
 *
 * Implements [94da5aad6c: Instrumentation.java#L1847-L1849](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Instrumentation.java#L1847-L1849)
 */
@Suppress("ClassName")
internal object InstrumentationPatch : IPatch {
	override fun install() {
		Instrumentation_execStartActivity.install()
		Instrumentation_checkStartActivityResult.install()
	}

	private data class SmuggledParams(val requestCode: Int, val options: Bundle?)

	/** Stores parameters smuggled via intents. */
	private val smuggledParameters: WeakHashMap<Intent, SmuggledParams> = WeakHashMap()

	private object Instrumentation_execStartActivity : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					Instrumentation::class, "execStartActivity",
					Context::class, IBinder::class, IBinder::class, Activity::class, Intent::class, Int::class,
					Bundle::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val intent = param.args[4] as Intent
			val requestCode = param.args[5] as Int
			val options = param.args[6] as Bundle?

			// attach parameters to `intent` for the `checkStartActivityResult` hook
			smuggledParameters[intent] = SmuggledParams(requestCode, options)
			Log.v(TAG, "execStartActivity: smuggling arguments via intent: requestCode = $requestCode, options = $options")
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			val intent = param.args[4] as Intent

			// cleanup smuggled parameters
			smuggledParameters.remove(intent)
		}
	}

	private object Instrumentation_checkStartActivityResult : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					Instrumentation::class, "checkStartActivityResult",
					Int::class, Object::class
				),
				this
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			// check is required because of `checkStartActivityResult` parameter type
			if (param.args[1] is Intent) {
				val intent = param.args[1] as Intent

				// extract smuggled parameters (skip hook if we don't have any)
				val params = smuggledParameters[intent] ?: return

				Log.v(TAG, "checkStartActivityResult: received smuggled arguments: requestCode = ${params.requestCode}, options = ${params.options}")

				// extract resultCode from hooked arguments
				val resultCode = param.args[0] as Int

				// finally call internal hook
				GmsHooks.onActivityStart(resultCode, intent, params.requestCode, params.options);
			}
		}
	}
}
