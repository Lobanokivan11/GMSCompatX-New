/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.provider.DeviceConfig
import android.provider.DeviceConfig.Properties
import com.android.internal.gmscompat.GmsCompatApp
import com.android.internal.gmscompat.GmsCompatApp.deviceConfigNamespace
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: intercept [DeviceConfig] interactions and redirect them to [GmsCompatApp].
 *
 * Implements the following changes:
 * - [94da5aad6c: DeviceConfig.java#L791](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/DeviceConfig.java#L791)
 * - [94da5aad6c: DeviceConfig.java#L861-865](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/DeviceConfig.java#L861-L865)
 * - [94da5aad6c: DeviceConfig.java#L974](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/DeviceConfig.java#L974)
 * - [94da5aad6c: DeviceConfig.java#L1000](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/DeviceConfig.java#L1000)
 * - [94da5aad6c: DeviceConfig.java#L1053](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/provider/DeviceConfig.java#L1053)
 */
@Suppress("ClassName")
internal object DeviceConfigPatch : IPatch {
	override fun install() {
		DeviceConfig_getProperty.install()
		DeviceConfig_getBoolean.install()
		DeviceConfig_setProperty.install()
		DeviceConfig_setProperties.install()
		DeviceConfig_resetToDefaults.install()
	}

	private object DeviceConfig_getProperty : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(DeviceConfig::class, "getProperty", String::class, String::class),
				this,
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam): String? = GmsCompatApp.getString(
			deviceConfigNamespace(param.args[0] as String),
			param.args[1] as String,
		)
	}

	private object DeviceConfig_getBoolean : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					DeviceConfig::class,
					"getBoolean",
					String::class,
					String::class,
					Boolean::class
				),
				this,
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val namespace = param.args[0] as String
			val name = param.args[1] as String
			val defaultValue = param.args[2] as Boolean

			// GOS: "Not overridden anywhere, but checked very often. Calls to GmsCompatApp are not cached, avoid IPC spam"
			if (namespace == "gservices" && name == "enable_gmscore_gservices_storage") {
				param.result = defaultValue
			}
		}
	}

	private object DeviceConfig_setProperty : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					DeviceConfig::class,
					"setProperty",
					String::class,
					String::class,
					String::class,
					Boolean::class
				),
				this,
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam): Boolean = GmsCompatApp.putString(
			deviceConfigNamespace(param.args[0] as String),
			param.args[1] as String,
			param.args[2] as String,
			// GOS: "makeDefault is ignored: defaults are unsupported by GmsCompat and are unused by GMS"
		)
	}

	private object DeviceConfig_setProperties : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(DeviceConfig::class, "setProperties", Properties::class),
				this,
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam): Boolean =
			GmsCompatApp.setProperties(param.args[0] as Properties)
	}

	private object DeviceConfig_resetToDefaults : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(DeviceConfig::class, "resetToDefaults", Int::class, String::class),
				this,
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam) =
			throw UnsupportedOperationException()
	}
}
