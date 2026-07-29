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
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.concurrent.Executor

/**
 * Purpose: initialize [BinderRedirector] when necessary by calling [BinderRedirector.maybeInit].
 *
 * Note: Refactored to use reflection to bypass NoClassDefFoundError on GrapheneOS system classes.
 */
@Suppress("ClassName")
internal object InitBinderRedirectorPatch : IPatch {
	private const val TAG = "GMSCompatX.InitBinderRedirectorPatch"
	private var binderRedirectorClass: Class<*>? = null
	private var isClassChecked = false

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
	 */
	private val currentlyBindingServices: MutableSet<CtxIntentEntry> = Collections.newSetFromMap(ConcurrentHashMap<CtxIntentEntry, Boolean>())

	private object ContextImpl_bindServiceCommon : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent
			currentlyBindingServices.add(CtxIntentEntry(ctx, service))
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent
			currentlyBindingServices.remove(CtxIntentEntry(ctx, service))
		}
	}

	private object ContextImpl_validateServiceIntent : XC_MethodHook() {
		override fun afterHookedMethod(param: MethodHookParam) {
			if (param.hasThrowable()) return

			val ctx = param.thisObject as Context
			val service = param.args[0] as Intent

			if (currentlyBindingServices.contains(CtxIntentEntry(ctx, service))) {
				if (!isClassChecked) {
					try {
						binderRedirectorClass = Class.forName("com.android.internal.gmscompat.BinderRedirector", false, ctx.classLoader)
					} catch (e: ClassNotFoundException) {
						Log.w(TAG, "GMSCompatX: com.android.internal.gmscompat.BinderRedirector not found. Skipping proxy call.")
					} finally {
						isClassChecked = true
					}
				}
				binderRedirectorClass?.let { targetClass ->
					try {
						XposedHelpers.callStaticMethod(targetClass, "maybeInit", service)
					} catch (t: Throwable) {
						Log.e(TAG, "GMSCompatX: Failed to execute BinderRedirector.maybeInit via reflection", t)
					}
				}
			}
		}
	}
}
