/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.UserHandle
import android.util.ArraySet
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections
import com.android.internal.gmscompat.BinderRedirector
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.concurrent.Executor

/**
 * Purpose: initialize [BinderRedirector] when necessary by calling [BinderRedirector.maybeInit].
 *
 * Implements: [94da5aad6c: ContextImpl.java#L2049](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L2049)
 *
 * Note: this implementation differs significantly from the original patch.
 */
@Suppress("ClassName")
internal object InitBinderRedirectorPatch : IPatch {
	override fun install() {
		val contextImpl = XposedHelpers.findClass("android.app.ContextImpl", null)

		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				contextImpl.kotlin, "bindServiceCommon",
				Intent::class, ServiceConnection::class, Int::class, String::class, Handler::class,
				Executor::class, UserHandle::class,
			),
			ContextImpl_bindServiceCommon,
		)
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(contextImpl.kotlin, "validateServiceIntent", Intent::class),
			ContextImpl_validateServiceIntent,
		)
	}


	/** Represents a context + service intent pair. */
	private data class CtxIntentEntry(val ctx: Context, val service: Intent) {
		override fun equals(other: Any?): Boolean {
			return if (other is CtxIntentEntry) {
				(ctx === other.ctx) && (service === other.service)
			} else {
				false
			}
		}

		override fun hashCode(): Int {
			var result = ctx.hashCode()
			result = 31 * result + service.hashCode()
			return result
		}
	}

	/**
	 * Tracks service [Intents][Intent] and their associated [Contexts][Context] while they are in `bindServiceCommon()`.
	 * Uses strong references - failure to drop entries will result in a memory leak!
	 */
	private val currentlyBindingServices = ArraySet<CtxIntentEntry>()


	private object ContextImpl_bindServiceCommon : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent

			// mark service intent for check
			currentlyBindingServices.add(CtxIntentEntry(ctx, service))
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent

			// unmark service intent
			currentlyBindingServices.remove(CtxIntentEntry(ctx, service))
		}
	}

	private object ContextImpl_validateServiceIntent : XC_MethodHook() {
		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent

			// check if this intent is from `bindServiceCommon`
			if (currentlyBindingServices.contains(CtxIntentEntry(ctx, service))) {
				BinderRedirector.maybeInit(service)
			}
		}
	}
}
