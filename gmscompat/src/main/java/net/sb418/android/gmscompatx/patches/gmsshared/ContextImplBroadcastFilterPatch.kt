/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.UserHandle
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: filter [ContextImpl.sendBroadcast] and similar methods through [GmsHooks.filterBroadcastOptions].
 *
 * Implements:
 * - [94da5aad6c: ContextImpl.java#L1293](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L1293)
 * - [94da5aad6c: ContextImpl.java#L1389](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L1389)
 * - [94da5aad6c: ContextImpl.java#L1448](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L1448)
 * - [94da5aad6c: ContextImpl.java#L1505](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L1505)
 */
internal object ContextImplBroadcastFilterPatch : IPatch {
	override fun install() {
		val classContextImpl = Class.forName("android.app.ContextImpl").kotlin

		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				classContextImpl, "sendBroadcast",
				Intent::class, String::class, Bundle::class
			),
			SendBroadcastHook(2)
		)
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				classContextImpl, "sendOrderedBroadcast",
				Intent::class, String::class, Int::class, BroadcastReceiver::class, Handler::class, Int::class,
				String::class, Bundle::class, Bundle::class
			),
			SendBroadcastHook(8)
		)
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				classContextImpl, "sendBroadcastAsUser",
				Intent::class, UserHandle::class, String::class, Bundle::class
			),
			SendBroadcastHook(3)
		)
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				classContextImpl, "sendOrderedBroadcastAsUser",
				Intent::class, UserHandle::class, String::class, Int::class, Bundle::class, BroadcastReceiver::class,
				Handler::class, Int::class, String::class, Bundle::class
			),
			SendBroadcastHook(4)
		)
	}

	/**
	 * @param optionIdx Index of the `options` parameter (type [Bundle]) for the hooked method.
	 */
	private class SendBroadcastHook(val optionIdx: Int) : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val intent = param.args[0] as Intent
			val options = param.args[optionIdx] as Bundle?
			param.args[optionIdx] = GmsHooks.filterBroadcastOptions(intent, options)
		}
	}
}
