/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: stub out privileged `LAUNCH_MULTI_PANE_SETTINGS_DEEP_LINK` activities.
 *
 * Implements: [94da5aad6c: Intent.java#L9475-L9478](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/Intent.java#L9475-L9478)
 */
internal object IntentPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(Intent::class, "resolveActivity", PackageManager::class),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val intent = param.thisObject as Intent

		// LAUNCH_MULTI_PANE_SETTINGS_DEEP_LINK permission has protectionLevel="signature|preinstalled"
		if (intent.action == Settings.ACTION_SETTINGS_EMBED_DEEP_LINK_ACTIVITY) {
			param.result = null
		}
	}
}
