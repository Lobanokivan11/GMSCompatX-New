/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.Settings
import android.util.ArraySet
import com.android.internal.gmscompat.GmsCompatApp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: intercept [Settings] interactions to:
 * 1. redirect GMS-specific settings to [GmsCompatApp]
 * 2. fail silently instead of throwing [SecurityException]s
 *
 * Implements:
 * - [94da5aad6c: Settings.java#L3015-L3019](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3015-L3019)
 * - [94da5aad6c: Settings.java#L3083-L3088](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3083-L3088)
 * - [94da5aad6c: Settings.java#L3099](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3099)
 * - [94da5aad6c: Settings.java#L3119](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3119)
 */
@Suppress("ClassName")
internal object SettingsPatch : IPatch {
	override fun install() {
		Settings_NameValueCache_putStringForUser.install()
		Settings_NameValueCache_getStringForUser.install()
	}

	@SuppressLint("PrivateApi")
	private val CLASS_NVC = Class.forName("android.provider.Settings\$NameValueCache").kotlin

	/**
	 * Helper method, originally defined on [Settings.NameValueCache].
	 */
	private fun maybeGetGmsCompatNamespace(nameValueCache: Any): String? {
		if (!CLASS_NVC.isInstance(nameValueCache))
			throw IllegalArgumentException()

		return when (XposedHelpers.getObjectField(nameValueCache, "mUri") as Uri) {
			Settings.Global.CONTENT_URI -> "global"
			Settings.Secure.CONTENT_URI -> "secure"
			else -> null
		}
	}

	/**
	 * Helper method (not required by original patch).
	 */
	private fun hasField(nameValueCache: Any, name: String): Boolean {
		if (!CLASS_NVC.isInstance(nameValueCache))
			throw IllegalArgumentException()

		@Suppress("UNCHECKED_CAST")  // safety: the inner type doesn't matter for `contains()`
		val mAllFields = XposedHelpers.getObjectField(nameValueCache, "mAllFields") as ArraySet<String>

		return mAllFields.contains(name)
	}

	private object Settings_NameValueCache_putStringForUser : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					CLASS_NVC,
					"putStringForUser",
					ContentResolver::class,
					String::class,
					String::class,
					String::class,
					Boolean::class,
					Int::class,
					Boolean::class,
				),
				this
			)
		}

		/**
		 * Implements [94da5aad6c: Settings.java#L3015-L3019]([94da5aad6c: Settings.java#L3015-L3019](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3015-L3019))
		 */
		override fun replaceHookedMethod(param: MethodHookParam): Boolean {
			val ns = maybeGetGmsCompatNamespace(param.thisObject)

			if (ns != null) {
				val name = param.args[1] as String
				val value = param.args[2] as String?

				if (!hasField(param.thisObject, name)) {
					return GmsCompatApp.putString(ns, name, value)
				}
			}

			return false
		}
	}

	private object Settings_NameValueCache_getStringForUser : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					CLASS_NVC,
					"getStringForUser",
					ContentResolver::class,
					String::class,
					Int::class,
				),
				this
			)
		}

		/**
		 * Implements [94da5aad6c: Settings.java:L3083-L3088](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3083-L3088)
		 */
		override fun beforeHookedMethod(param: MethodHookParam) {
			val ns = maybeGetGmsCompatNamespace(param.thisObject)

			if (ns != null) {
				val name = param.args[1] as String

				if (!hasField(param.thisObject, name) && !name.startsWith("gmscompat")) {
					param.result = GmsCompatApp.getString(ns, name)
				}
			}
		}

		/**
		 * Implements:
		 * - [94da5aad6c: Settings.java:L3099](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3099)
		 * - [94da5aad6c: Settings.java:L3119](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/Settings.java#L3119)
		 */
		override fun afterHookedMethod(param: MethodHookParam) {
			// replace SecurityExceptions with null return
			if (param.throwable is SecurityException) {
				param.throwable = null
			}
		}
	}
}
