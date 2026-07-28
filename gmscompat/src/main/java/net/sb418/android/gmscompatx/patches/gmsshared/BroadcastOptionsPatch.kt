/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.BroadcastOptions
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: Stub out `BroadcastOptions.{setBackgroundActivityStartsAllowed,recordResponseEventWhileInBackground}`
 * methods.
 *
 * Implements:
 * - [94da5aad6c: BroadcastOptions.java#L432](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/BroadcastOptions.java#L432)
 * - [94da5aad6c: BroadcastOptions.java#L595](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/BroadcastOptions.java#L595)
 */
internal object BroadcastOptionsPatch : IPatch {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				BroadcastOptions::class,
				"setBackgroundActivityStartsAllowed",
				Boolean::class
			),
			XC_MethodReplacement.DO_NOTHING
		)

		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				BroadcastOptions::class,
				"recordResponseEventWhileInBackground",
				Long::class
			),
			XC_MethodReplacement.DO_NOTHING
		)
	}
}
