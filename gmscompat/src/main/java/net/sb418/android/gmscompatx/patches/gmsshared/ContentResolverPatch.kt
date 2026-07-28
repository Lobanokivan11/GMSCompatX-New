/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import com.android.internal.gmscompat.GmsCompatApp
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.GMSPatches.TAG
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: patch multiple [ContentResolver] methods. See each hook's documentation for details.
 *
 * Implements:
 * - [94da5aad6c: ContentResolver.java#L1268-L1269](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L1268-L1269)
 * - [94da5aad6c: ContentResolver.java#L1255-L1258](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L1255-L1258)
 * - [94da5aad6c: ContentResolver.java#L2201](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2201)
 * - [94da5aad6c: ContentResolver.java#L2585](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2585)
 * - [94da5aad6c: ContentResolver.java#L2748](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2748)
 * - [94da5aad6c: ContentResolver.java#L2771](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2771)
 */
@Suppress("ClassName")
internal object ContentResolverPatch : IPatch {
	override fun install() {
		ContentResolver_query.install()
		ContentResolver_insert.install()
		ContentResolver_acquireContentProviderClient.install()
		ContentResolver_registerContentObserver.install()
		ContentResolver_unregisterContentObserver.install()
	}

	/**
	 * Purpose:
	 * 1. suppress [SecurityException]s
	 * 2. filter [query][ContentResolver.query] results through [GmsHooks.maybeModifyQueryResult]
	 *
	 * Implements:
	 * - [94da5aad6c: ContentResolver.java#L1268-L1269](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L1268-L1269)
	 * - [94da5aad6c: ContentResolver.java#L1255-L1258](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L1255-L1258)
	 */
	private object ContentResolver_query : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ContentResolver::class, "query",
					Uri::class, Array<String>::class, Bundle::class, CancellationSignal::class
				),
				this
			)
		}

		/**
		 * Hooked method is recursive - track recursion depth so we only act on the final returned value.
		 *
		 * We use our full class name as the method ID to avoid clashes.
		 */
		private val METHOD_COUNTER_ID = javaClass.name

		override fun beforeHookedMethod(param: MethodHookParam?) {
			XposedHelpers.incrementMethodDepth(METHOD_COUNTER_ID)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			XposedHelpers.decrementMethodDepth(METHOD_COUNTER_ID)

			if (param.hasThrowable()) {
				// suppress security exceptions
				if (param.throwable is SecurityException) {
					Log.d(TAG, "suppressed SecurityException", param.throwable)
					param.throwable = null
				}

				// don't call hook
				return
			}

			// only call hook once
			if (XposedHelpers.getMethodDepth(METHOD_COUNTER_ID) == 0) {
				if (param.result == null)
					return

				val modified = GmsHooks.maybeModifyQueryResult(
					param.args[0] as Uri,
					param.args[1] as Array<String>?,
					param.args[2] as Bundle?,
					param.result as Cursor,
				)

				if (modified != null)
					param.result = modified
			}
		}
	}

	/**
	 * Purpose: filter [ContentResolver.insert] calls through [GmsHooks.filterContentValues].
	 *
	 * Implements: [94da5aad6c: ContentResolver.java#L2201](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2201)
	 */
	private object ContentResolver_insert : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ContentResolver::class, "insert",
					Uri::class, ContentValues::class, Bundle::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val url = param.args[0] as Uri?
			val values = param.args[1] as ContentValues?

			if (url != null) {
				// modifies values in-place
				GmsHooks.filterContentValues(url, values)
			}
		}
	}

	/**
	 * Purpose: suppress [SecurityException]s from [ContentResolver.acquireContentProviderClient].
	 *
	 * Implements: [94da5aad6c: ContentResolver.java#L2585](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2585)
	 */
	private object ContentResolver_acquireContentProviderClient : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(ContentResolver::class, "acquireContentProviderClient", Uri::class),
				this
			)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			if (param.throwable is SecurityException) {
				Log.d(TAG, "uri: ${param.args[0] as Uri?}", param.throwable)
				param.throwable = null
			}
		}
	}

	/**
	 * Purpose: intercept `registerContentObserver` calls according to [GmsCompatApp.registerObserver].
	 *
	 * Implements: [94da5aad6c: ContentResolver.java#L2748](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2748)
	 */
	private object ContentResolver_registerContentObserver : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ContentResolver::class, "registerContentObserver",
					Uri::class, Boolean::class, ContentObserver::class, Int::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val uri = param.args[0] as Uri
			val observer = param.args[2] as ContentObserver

			if (GmsCompatApp.registerObserver(uri, observer)) {
				param.result = null
			}
		}
	}

	/**
	 * Purpose: intercept `unregisterContentObserver` calls according to [GmsCompatApp.unregisterObserver].
	 *
	 * Implements: [94da5aad6c: ContentResolver.java#L2771](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/ContentResolver.java#L2771)
	 */
	private object ContentResolver_unregisterContentObserver : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					ContentResolver::class, "unregisterContentObserver",
					ContentObserver::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val observer = param.args[0] as ContentObserver

			if (GmsCompatApp.unregisterObserver(observer)) {
				param.result = null
			}
		}
	}
}
