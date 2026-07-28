/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.os.PowerExemptionManager
import com.android.internal.gmscompat.client.ClientPriorityManager
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: intercept attempts to (temporarily) exempt the app from power saving, and relay them to
 * [ClientPriorityManager.raiseToForeground] instead.
 *
 * Implements [94da5aad6c: PowerExemptionManager.java#L586](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/apex/jobscheduler/framework/java/android/os/PowerExemptionManager.java#L586)
 */
internal object PowerExemptionManagerPatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				PowerExemptionManager::class, "addToTemporaryAllowList",
				String::class, Int::class, String::class, Long::class
			),
			this
		)
	}

	override fun replaceHookedMethod(param: MethodHookParam): Nothing? {
		val packageName = param.args[0] as String
		val reasonCode = param.args[1] as Int
		val reason = param.args[2] as String?
		val durationMs = param.args[3] as Long

		ClientPriorityManager.raiseToForeground(packageName, durationMs, reason, reasonCode)

		return null
	}
}
