/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder


/**
 * Purpose: filter [PendingIntent.send] through [GmsHooks.filterBroadcastOptions].
 *
 * Implements [94da5aad6c: PendingIntent.java#L989-L996](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/PendingIntent.java#L989-L996)
 */
internal object PendingIntentPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				PendingIntent::class,
				"send",
				Context::class,
				Int::class,
				Intent::class,
				PendingIntent.OnFinished::class,
				Handler::class,
				String::class,
				Bundle::class,
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val thisObj = param.thisObject as PendingIntent
		val intent = param.args[2] as Intent?
		val options = param.args[6] as Bundle?

		if (intent != null && options != null && thisObj.isBroadcast) {
			val targetPkg = thisObj.creatorPackage
			if (targetPkg != null) {
				param.args[6] = GmsHooks.filterBroadcastOptions(options, targetPkg)
			}
		}
	}
}
