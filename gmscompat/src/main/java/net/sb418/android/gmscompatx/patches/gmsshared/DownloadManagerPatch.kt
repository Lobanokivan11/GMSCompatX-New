/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.DownloadManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: skip setting downloads as hidden (requires `DOWNLOAD_WITHOUT_NOTIFICATION` permission).
 *
 * Implements: [94da5aad6c: DownloadManager.java#L715](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/DownloadManager.java#L715)
 */
internal object DownloadManagerPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(DownloadManager.Request::class, "setNotificationVisibility", Int::class),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val visibility = param.args[0] as Int

		if (visibility == DownloadManager.Request.VISIBILITY_HIDDEN) {
			// skip builder method by returning ourselves
			param.result = param.thisObject
		}
	}
}
