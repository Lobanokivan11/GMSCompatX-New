/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.app.AppComponentFactory
import android.content.Intent
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: call [GmsHooks.maybeInstantiateService] to create our custom services when requested.
 *
 * Implements: [94da5aad6c: ActivityThread.java#L4464-L4471](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ActivityThread.java#L4464-L4471)
 *
 * XXX: this hook may fail:
 * if the app overrides `#instantiateService` without calling `super()`, the hook will not trigger.
 */
internal object ServiceCreationPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				AppComponentFactory::class, "instantiateService",
				ClassLoader::class, String::class, Intent::class),
			this,
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val className = param.args[1] as String

		val service = GmsHooks.maybeInstantiateService(className)
		if (service != null) {
			param.result = service
		}
	}
}
