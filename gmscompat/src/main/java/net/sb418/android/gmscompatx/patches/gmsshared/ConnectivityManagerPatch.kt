/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.net.ConnectivityManager
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: stub out privileged `isTetheringSupported` method.
 *
 * Implements: [modules/Connectivity: 4edba19338](https://github.com/GrapheneOS/platform_packages_modules_Connectivity/commit/4edba19338c32144b48c5f1bd62150dcc7c37213)
 */
internal object ConnectivityManagerPatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(ConnectivityManager::class, "isTetheringSupported"),
			this
		)
	}

	override fun replaceHookedMethod(param: MethodHookParam): Boolean = false
}
