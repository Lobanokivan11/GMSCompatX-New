/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.annotation.SuppressLint
import android.app.ActivityManagerHidden.START_ABORTED
import android.app.ActivityManagerHidden.START_SUCCESS
import android.app.compat.gms.GmsCompat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import net.sb418.android.gmscompatx.util.SyncWeakHashSet

/**
 * Purpose: expose true result of activity start to GMS apps (which would normally be masked)
 *
 * Implements: [3ac920ce4c: ActivityStarter.java#L741-L745](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/services/core/java/com/android/server/wm/ActivityStarter.java#L741-L745)
 *
 * XXX: Assumes that [ActivityStarter.execute] never runs concurrently on the same object.
 */
@Suppress("ClassName")
internal object ActivityStarterPatch : IPatch {
	override fun install() {
		ActivityStarter_execute.install()
		ActivityStarter_executeRequest.install()
	}

	/**
	 * Tracks calls to [ActivityStarter.execute] which should return `START_ABORTED`.
	 *
	 * Type should be `com.android.server.wm.ActivityStarter`, but it isn't public.
	 */
	private val ABORTED_STARTS = SyncWeakHashSet<Any>(4)

	@SuppressLint("PrivateApi")
	private val classActivityStarter = Class.forName("com.android.server.wm.ActivityStarter").kotlin

	@SuppressLint("PrivateApi")
	private val classRequest = Class.forName("com.android.server.wm.ActivityStarter\$Request").kotlin

	private object ActivityStarter_execute : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(classActivityStarter, "execute"), this)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			// check if we need to override the return value
			if (param.result == START_SUCCESS && ABORTED_STARTS.contains(param.thisObject)) {
				param.result = START_ABORTED
			}

			// cleanup
			ABORTED_STARTS.remove(param.thisObject)
		}
	}

	private object ActivityStarter_executeRequest : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(classActivityStarter, "executeRequest", classRequest),
				this
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			if (param.result as Int == START_ABORTED) {
				val mRequest = param.args[0]
				val callingPackage = XposedHelpers.getObjectField(mRequest, "callingPackage") as String
				val userId = XposedHelpers.getIntField(mRequest, "userId")

				if (GmsCompat.isGmsApp(callingPackage, userId)) {
					ABORTED_STARTS.add(param.thisObject)
				}
			}
		}
	}
}
