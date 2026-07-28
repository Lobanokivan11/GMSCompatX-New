/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.app.Activity
import android.app.ActivityThread
import android.app.compat.gms.GmsCompat
import android.content.ContextHidden
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import com.android.internal.gmscompat.GmsCompatApp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: trigger "missing app" notifications for optional Google apps when
 * other apps attempt to start their activities.
 *
 * Implements: [94da5aad6c: Activity.java#L5521-L5548](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/Activity.java#L5521-L5548)
 *
 * Note: priority is lowered for consistent ordering with the GmsCompat hook on the same method.
 */
internal object ActivityPatch : IPatch, XC_MethodHook(PRIORITY_DEFAULT - 1000) {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(Activity::class, "startActivityForResult",
				Intent::class, Int::class, Bundle::class),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val intent = param.args[0] as Intent

		if (intent.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
			val data = intent.data
			if (data != null && data.scheme == "package") {
				val ctx = param.thisObject as ContextHidden
				when (val pkg = data.schemeSpecificPart) {
					"com.google.android.tts" -> {
						if (GmsCompat.isClientOfGmsCore()) {
							val installed: Boolean
							try {
								installed = ActivityThread.getPackageManager().getApplicationInfo(pkg, 0, ctx.userId) != null
							} catch (e: RemoteException) {
								throw e.rethrowFromSystemServer()
							}

							if (!installed) {
								try {
									GmsCompatApp.iClientOfGmsCore2Gca().showMissingAppNotification(pkg)
								} catch (e: RemoteException) {
									GmsCompatApp.callFailed(e)
								}
							}
						}
					}
				}
			}
		}
	}
}
