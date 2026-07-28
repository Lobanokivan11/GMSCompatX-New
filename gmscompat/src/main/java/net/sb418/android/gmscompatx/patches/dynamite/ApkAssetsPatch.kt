/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.dynamite

import android.content.res.ApkAssets
import android.content.res.loader.AssetsProvider
import com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.io.IOException

/**
 * Purpose: hook [ApkAssets.loadFromPath] to handle loading Dynamite modules ourselves.
 *
 * Implements:
 * - [94da5aad6c: ApkAssets.java#L146](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/res/ApkAssets.java#L146)
 * - [94da5aad6c: ApkAssets.java#L160-L165](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/res/ApkAssets.java#L160-L165)
 */
internal object ApkAssetsPatch : IPatch, XC_MethodHook() {
	override fun install() {
		// hook both overloads
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				ApkAssets::class, "loadFromPath", String::class, Int::class
			),
			this
		)
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				ApkAssets::class, "loadFromPath", String::class, Int::class, AssetsProvider::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val path = param.args[0] as String
		val flags = param.args[1] as Int
		val assets = if (param.args.size == 3) param.args[2] as AssetsProvider? else null

		try {
			val ret = GmsDynamiteClientHooks.loadAssetsFromPath(path, flags, assets)
			if (ret != null)
				param.result = ret
		} catch (e: IOException) {
			param.throwable = e
		}
	}
}
