/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.os.Build
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: spoof the hardware serial number.
 *
 * Implements [94da5aad6c: Build.java#L207-L209](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Build.java#L207-L209)
 */
internal object BuildPatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(Build::class, "getSerial"),
			this
		)
	}

	override fun replaceHookedMethod(param: MethodHookParam): String =
		GmsHooks.getSerial()
}
