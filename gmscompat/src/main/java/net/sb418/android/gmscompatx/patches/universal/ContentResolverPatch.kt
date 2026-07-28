/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.content.ContentResolver
import android.net.Uri
import com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: call [GmsDynamiteClientHooks.maybeInit] from [ContentResolver.acquireProvider].
 *
 * Implements: [94da5aad6c: ContentResolver.java#L2500](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2500)
 */
internal object ContentResolverPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(ContentResolver::class, "acquireProvider", Uri::class), this)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val uri = param.args[0] as Uri

		if (uri.scheme == ContentResolver.SCHEME_CONTENT && uri.authority != null) {
			GmsDynamiteClientHooks.maybeInit(uri.authority)
		}
	}
}
